/*
 * Copyright (C) 2023 David Schwingenschlögl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.oevsv.sota.data.persistence;

import at.oevsv.sota.data.WireMockExtension;
import at.oevsv.sota.data.api.Candidate;
import at.oevsv.sota.data.api.DiplomaRequest;
import at.oevsv.sota.data.api.Requester;
import at.oevsv.sota.data.api.SignedCandidate;
import at.oevsv.sota.data.domain.Summit;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@QuarkusTestResource(WireMockExtension.class)
final class DiplomaLogTest {

    @Inject
    DiplomaLogResource sut;

    @Test
    void alreadyRequested_yieldsTrueAfterCreate() {
        final var requester = new Requester("OE5IDT", "test@nothing.com", "Max Mustermann");
        final var candidate = new Candidate("OE5IDT", "1", Candidate.Category.S2S, Candidate.Rank.BRONZE, Map.of());

        sut.create(new DiplomaRequest(requester, Set.of(SignedCandidate.sign(candidate)), null));

        assertThat(sut.alreadyRequested(requester, candidate)).isTrue();
    }

    @Test
    void alreadyRequested_specialDiploma_yieldsTrueAfterCreateEvenIfRankMayChange() {
        final var requester = new Requester("OE5IDT", "test@nothing.com", "Max Mustermann");
        final var candidate = new Candidate("OE5IDT", "1", Candidate.Category.OE20SOTA, Candidate.Rank.BRONZE, Map.of(Summit.State.OE1, 20L));

        sut.create(new DiplomaRequest(requester, Set.of(SignedCandidate.sign(candidate)), null));

        assertThat(sut.alreadyRequested(requester, candidate)).isTrue();
        assertThat(sut.alreadyRequested(requester, new Candidate("OE5IDT", "1", Candidate.Category.OE20SOTA, Candidate.Rank.GOLD, Map.of(Summit.State.OE1, 20L, Summit.State.OE2, 1L)))).isTrue();
    }

    @Test
    void alreadyRequested_canonicalCallSign_yieldsTrueAfterCreate() {
        final var requester = new Requester("OE5IDT/p", "test@nothing.com", "Max Mustermann");
        final var candidate = new Candidate("OE5IDT", "1", Candidate.Category.S2S, Candidate.Rank.BRONZE, Map.of());

        sut.create(new DiplomaRequest(requester, Set.of(SignedCandidate.sign(candidate)), null));

        assertThat(sut.alreadyRequested(new Requester("oe5idt/3", "test@nothing.com", "Max Mustermann"), candidate)).isTrue();
    }

    @Test
    void alreadyRequested_yieldsFalseIfNotFound() {
        assertThat(sut.alreadyRequested(new Requester("OE5IDT", "test@nothing.com", "Max Mustermann"), new Candidate("OE5IDT", "1", Candidate.Category.CHASER, Candidate.Rank.BRONZE, Map.of()))).isFalse();
    }

    @Test
    void creatingSameCandidateTwiceDoesNothingTheSecondTime() {
        final var requester = new Requester("OE5IDT", "test@nothing.com", "Max Mustermann");
        final var candidate = new Candidate("OE5IDT", "1", Candidate.Category.ACTIVATOR, Candidate.Rank.BRONZE, Map.of());
        final var diplomaRequest = new DiplomaRequest(requester, Set.of(SignedCandidate.sign(candidate)), null);

        assertThat(sut.create(diplomaRequest)).isTrue();
        assertThat(sut.create(diplomaRequest)).isFalse();
    }
}

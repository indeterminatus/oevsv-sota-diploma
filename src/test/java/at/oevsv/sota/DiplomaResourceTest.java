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

package at.oevsv.sota;

import at.oevsv.sota.data.WireMockExtension;
import at.oevsv.sota.data.api.Candidate;
import at.oevsv.sota.data.api.DiplomaRequest;
import at.oevsv.sota.data.api.Requester;
import at.oevsv.sota.data.api.SignedCandidate;
import at.oevsv.sota.data.domain.Summit;
import at.oevsv.sota.data.persistence.DiplomaLogResource;
import at.oevsv.sota.data.persistence.DiplomaLogResourceTestSeam;
import at.oevsv.sota.data.persistence.SummitList;
import at.oevsv.sota.data.persistence.SummitListTestSeam;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.annotation.concurrent.GuardedBy;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.util.Collection;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@QuarkusTest
@QuarkusTestResource(WireMockExtension.class)
final class DiplomaResourceTest {

    @Inject
    DiplomaResource sut;

    @Inject
    SummitList summitList;

    @Inject
    DiplomaLogResource logs;

    private static final Object LOCK = new Object();

    @BeforeEach
    void synchronize() {
        SummitListTestSeam.synchronizeOn(summitList);
    }

    @Test
    void activators_fetched() {
        assertThat(sut.fetchActivators()).isNotEmpty();
    }

    @Test
    void chasers_fetched() {
        assertThat(sut.fetchChasers()).isNotEmpty();
    }

    @Test
    void summitList_fetched() {
        assertThat(sut.summitList()).isNotEmpty();
    }

    @Test
    void userId_canBeResolved() {
        assertThat(sut.userIdForCallSign("OE5JFE")).isEqualTo("11330");
    }

    @Test
    void userId_caseIsInsensitive() {
        assertThat(sut.userIdForCallSign("oe5JfE")).isEqualTo("11330");
    }

    @Test
    void userId_suffixIsIgnored() {
        assertThat(sut.userIdForCallSign("OE5JFE/p")).isEqualTo("11330");
    }

    @Test
    @GuardedBy("LOCK")
    void candidates_OE5JFE_yieldExpectedResults() {
        synchronized (LOCK) {
            DiplomaLogResourceTestSeam.deleteAllOn(logs);
            final var candidates = sut.checkCandidatesForUser("OE5JFE", null);

            assertActivatorCandidate(candidates);
            assertChaserCandidate(candidates);
        }
    }

    @Test
    void activatorLog_OE9NAT_yieldsExpectedResults() {
        final var candidates = sut.checkCandidatesForUser("OE9NAT", null);

        assertThat(candidates).isNotEmpty();
    }

    @Test
    void candidates_areStableAcrossMultipleCalls() {
        final var candidates = sut.checkCandidatesForUser("OE5JFE", null);
        final var secondFetch = sut.checkCandidatesForUser("OE5JFE", null);
        assertThat(secondFetch).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(candidates);
    }

    @Test
    @GuardedBy("LOCK")
    void candidates_requestDiploma() {
        synchronized (LOCK) {
            final var candidates = sut.checkCandidatesForUser("OE5JFE", null);
            final var request = sut.requestDiploma("OE5JFE", new DiplomaRequest(new Requester("OE5JFE", "noreply@nothing.com", "Dip-Dip Dabbadudei"), candidates, "de"));
            assertThat(request).isTrue();

            assertThat(DiplomaLogResourceTestSeam.listPendingOn(logs)).isNotEmpty();
            DiplomaLogResourceTestSeam.deleteAllOn(logs);
        }
    }

    @Test
    void candidates_nonExistingUser_throws() {
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> sut.checkCandidatesForUser("OE1QSO", null));
    }

    private static void assertActivatorCandidate(Collection<SignedCandidate> candidates) {
        assertThat(candidates).isNotEmpty().anySatisfy(signed -> {
            final var candidate = signed.candidate();
            assertThat(candidate).extracting(Candidate::category).isEqualTo(Candidate.Category.ACTIVATOR);
            assertThat(candidate).extracting(Candidate::callSign).isEqualTo("OE5JFE");
            assertThat(candidate).extracting(Candidate::userID).isEqualTo("11330");
            assertThat(candidate.activations()).containsEntry(Summit.State.OE1, 1L);
            assertThat(candidate.activations()).containsEntry(Summit.State.OE2, 19L);
            assertThat(candidate.activations()).containsEntry(Summit.State.OE3, 11L);
            assertThat(candidate.activations()).containsEntry(Summit.State.OE4, 1L);
            assertThat(candidate.activations()).containsEntry(Summit.State.OE5, 212L);
            assertThat(candidate.activations()).containsEntry(Summit.State.OE6, 39L);
            assertThat(candidate.activations()).containsEntry(Summit.State.OE7, 5L);
            assertThat(candidate.activations()).containsEntry(Summit.State.OE8, 7L);
            assertThat(candidate.activations()).containsEntry(Summit.State.OE9, 1L);
        });
    }

    private static void assertChaserCandidate(Collection<SignedCandidate> candidates) {
        assertThat(candidates).isNotEmpty().anySatisfy(signed -> {
            final var candidate = signed.candidate();
            assertThat(candidate).extracting(Candidate::category).isEqualTo(Candidate.Category.CHASER);
            assertThat(candidate).extracting(Candidate::callSign).isEqualTo("OE5JFE");
            assertThat(candidate).extracting(Candidate::userID).isEqualTo("11330");
            assertThat(candidate.activations()).containsEntry(Summit.State.OE1, 7L);
            assertThat(candidate.activations()).containsEntry(Summit.State.OE2, 90L);
            assertThat(candidate.activations()).containsEntry(Summit.State.OE3, 141L);
            assertThat(candidate.activations()).doesNotContainKey(Summit.State.OE4);
            assertThat(candidate.activations()).containsEntry(Summit.State.OE5, 420L);
            assertThat(candidate.activations()).containsEntry(Summit.State.OE6, 107L);
            assertThat(candidate.activations()).containsEntry(Summit.State.OE7, 17L);
            assertThat(candidate.activations()).containsEntry(Summit.State.OE8, 9L);
            assertThat(candidate.activations()).containsEntry(Summit.State.OE9, 4L);
        });
    }
}

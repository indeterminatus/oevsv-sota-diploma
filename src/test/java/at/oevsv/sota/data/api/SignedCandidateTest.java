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

package at.oevsv.sota.data.api;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.Assertions.assertThatNoException;

final class SignedCandidateTest {

    @Test
    void signedCandidate_canBeCreated() {
        final var signed = SignedCandidate.sign(candidate("OE5IDT"));
        assertThat(signed).isNotNull();
        assertThat(signed.candidate()).isNotNull();
        assertThat(signed.signature()).isNotNull();
    }

    @Test
    void signedCandidate_verifyIntegrity_positive() {
        final var signed = SignedCandidate.sign(candidate("OE5IDT"));
        assertThatNoException().isThrownBy(signed::verifyIntegrity);
    }

    @Test
    void signedCandidate_tamperedData_verifyIntegrity_fails() {
        final var signed = SignedCandidate.sign(candidate("OE5IDT"));
        final var tampered = new SignedCandidate(candidate("OE5JFE"), signed.signature());
        assertThatException().isThrownBy(tampered::verifyIntegrity);
    }

    @NotNull
    private static Candidate candidate(String callSign) {
        return new Candidate(callSign, "123", Candidate.Category.S2S, Candidate.Rank.GOLD, Map.of());
    }
}

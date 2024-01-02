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

package at.oevsv.sota.pdf;

import at.oevsv.sota.data.api.Candidate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

final class DefaultDiplomaIdGeneratorTest {

    private final Candidate candidate = new Candidate("XXX", null, Candidate.Category.S2S, Candidate.Rank.BRONZE, Map.of());

    @Test
    void generateIsStable() {
        final var sut = new DefaultDiplomaIdGenerator(candidate, 1);

        final var result0 = sut.generateId();
        final var result1 = sut.generateId();

        assertThat(result0).isEqualTo(result1);
    }

    @Test
    void checkOverflow() {
        final var sut = new DefaultDiplomaIdGenerator(candidate, 10_000);
        assertThat(sut.generateId()).isEqualTo("S2S-BR-10000");
    }

    @SuppressWarnings("DataFlowIssue") // justification: passing bad value is the point of this test
    @Test
    void negativeSequenceIsForbidden() {
        assertThatIllegalArgumentException().isThrownBy(() -> new DefaultDiplomaIdGenerator(candidate, -1));
    }

    @SuppressWarnings("DataFlowIssue") // justification: passing bad value is the point of this test
    @Test
    void sequenceWithTooManyDigitsIsForbidden() {
        assertThatIllegalArgumentException().isThrownBy(() -> new DefaultDiplomaIdGenerator(candidate, 100_000));
    }

    @Test
    void checkSuffix() {
        final var sut = new DefaultDiplomaIdGenerator(candidate, 10_000, "x");
        assertThat(sut.generateId()).isEqualTo("S2S-BR-10000x");
    }

    @Test
    void suffixCannotExceedLengthLimit() {
        assertThatIllegalArgumentException().isThrownBy(() -> new DefaultDiplomaIdGenerator(candidate, 1, "xx"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9})
    void suffixCannotContainDigits(int digit) {
        assertThatIllegalArgumentException().isThrownBy(() -> new DefaultDiplomaIdGenerator(candidate, 1, "" + digit));
    }
}

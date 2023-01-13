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

package at.oevsv.sota.data.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

final class SummitTest {

    private static Stream<Arguments> summitsAndStates() {
        //@formatter:off
        return Stream.of(
                Arguments.of("OE/WI-277", Summit.State.OE1),
                Arguments.of("oE/Wi-277", Summit.State.OE1),
                Arguments.of("OE/TI-111", Summit.State.OE7),
                Arguments.of("OE/TL-111", Summit.State.OE7),
                Arguments.of("I/LO-243", null)
        );
        //@formatter:on
    }

    @ParameterizedTest
    @MethodSource("summitsAndStates")
    void stateIsExtractedFromSummitCode(String summitCode, Summit.State expectedState) {
        assertThat(Summit.State.stateForSummitCode(summitCode)).isEqualTo(expectedState);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9})
    void stateIsExtractedFromOrdinalValue(int ordinal) {
        assertThat(Summit.State.stateForOrdinal(ordinal)).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 10, 11})
    void stateIsNullForOutOfBoundsOrdinalValue(int ordinal) {
        assertThat(Summit.State.stateForOrdinal(ordinal)).isNull();
    }
}

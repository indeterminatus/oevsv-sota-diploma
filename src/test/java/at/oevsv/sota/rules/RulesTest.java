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

package at.oevsv.sota.rules;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.time.Month;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

final class RulesTest {

    private static Stream<Arguments> date_pairs() {
        //@formatter:off
        return Stream.of(
                Arguments.of(LocalDate.of(2021, Month.JANUARY, 1), LocalDate.of(2021, Month.JANUARY, 1), true),
                Arguments.of(LocalDate.of(2021, Month.JANUARY, 1), null, true),
                Arguments.of(LocalDate.of(2021, Month.JANUARY, 1), LocalDate.of(2021, Month.JANUARY, 2), false),
                Arguments.of(LocalDate.of(2021, Month.JANUARY, 1), LocalDate.of(2021, Month.FEBRUARY, 1), false),
                Arguments.of(LocalDate.of(2021, Month.JANUARY, 1), LocalDate.of(2020, Month.DECEMBER, 31), true)
        );
        //@formatter:on
    }

    @ParameterizedTest
    @MethodSource("date_pairs")
    void withinTimeRange(LocalDate actual, LocalDate required, boolean expectedMatch) {
        assertThat(Rules.isWithinTimeRange(actual, new Rules.CommonArguments("", "", Map.of(), required))).isEqualTo(expectedMatch);
    }
}

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

package at.oevsv.sota.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatRuntimeException;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class YearAwareFetcherTest {

    // region testing yearParametersFor
    @Test
    void yearParametersFor_reference_before_checkAfter_isFallback() {
        final var checkAfter = LocalDate.of(2024, Month.JANUARY, 1);
        final var reference = LocalDate.of(2023, Month.JANUARY, 1);
        assertThat(YearAwareFetcher.yearParametersFor(checkAfter, reference)).containsExactly("all");
    }

    @Test
    void yearParametersFor_checkAfter_notSpecified_isFallback() {
        assertThat(YearAwareFetcher.yearParametersFor(null, LocalDate.of(2023, Month.JANUARY, 1))).containsExactly("all");
    }

    @Test
    void yearParametersFor_currentYear_containsOnlyThat() {
        final var checkAfter = LocalDate.of(2023, Month.JANUARY, 1);
        final var reference = LocalDate.of(2023, Month.MARCH, 11);
        assertThat(YearAwareFetcher.yearParametersFor(checkAfter, reference)).containsExactly("2023");
    }

    @Test
    void yearParametersFor_pastYear_containsCurrentAndLast() {
        final var checkAfter = LocalDate.of(2022, Month.JANUARY, 1);
        final var reference = LocalDate.of(2023, Month.MARCH, 11);
        assertThat(YearAwareFetcher.yearParametersFor(checkAfter, reference)).containsExactly("2022", "2023");
    }

    @Test
    void yearParametersFor_tenYearsDifference() {
        final var checkAfter = LocalDate.of(2000, Month.JANUARY, 1);
        final var reference = LocalDate.of(2010, Month.MARCH, 11);
        assertThat(YearAwareFetcher.yearParametersFor(checkAfter, reference)).containsExactly("2000", "2001", "2002", "2003", "2004", "2005", "2006", "2007", "2008", "2009", "2010");
    }

    @Test
    void yearParametersFor_moreThanTenYears_isFallback() {
        final var checkAfter = LocalDate.of(2000, Month.JANUARY, 1);
        final var reference = LocalDate.of(2011, Month.MARCH, 11);
        assertThat(YearAwareFetcher.yearParametersFor(checkAfter, reference)).containsExactly("all");
    }
    // endregion

    @Test
    void combineResultsForEveryYear_single(@Mock Function<String, Collection<String>> sensor) {
        when(sensor.apply("2023")).thenReturn(List.of("test", "test"));
        final var checkAfter = LocalDate.of(2023, Month.JANUARY, 1);
        final var reference = LocalDate.of(2023, Month.MARCH, 11);

        final var combined = YearAwareFetcher.combineResultsForEveryYear(checkAfter, reference, sensor);

        assertThat(combined).containsExactly("test", "test");
        verify(sensor).apply("2023");
    }

    @Test
    void combineResultsForEveryYear_multiple(@Mock Function<String, Collection<String>> sensor) {
        when(sensor.apply("2022")).thenReturn(List.of("test2022_1", "test2022_2"));
        when(sensor.apply("2023")).thenReturn(List.of("test2023_1", "test2023_2"));
        final var checkAfter = LocalDate.of(2022, Month.JANUARY, 1);
        final var reference = LocalDate.of(2023, Month.MARCH, 11);

        final var combined = YearAwareFetcher.combineResultsForEveryYear(checkAfter, reference, sensor);

        assertThat(combined).containsExactly("test2022_1", "test2022_2", "test2023_1", "test2023_2");
        verify(sensor).apply("2022");
        verify(sensor).apply("2023");
    }

    @Test
    void combineResultsForEveryYear_exceptionInLastCall_bubbles(@Mock Function<String, Collection<String>> sensor) {
        when(sensor.apply("2022")).thenReturn(List.of("test2022_1", "test2022_2"));
        when(sensor.apply("2023")).thenThrow(new RuntimeException("SABOTAGE!"));
        final var checkAfter = LocalDate.of(2022, Month.JANUARY, 1);
        final var reference = LocalDate.of(2023, Month.MARCH, 11);

        assertThatRuntimeException().isThrownBy(() -> YearAwareFetcher.combineResultsForEveryYear(checkAfter, reference, sensor)).withMessage("SABOTAGE!");

        verify(sensor).apply("2022");
        verify(sensor).apply("2023");
    }

    @Test
    void combineResultsForEveryYear_exceptionInFirstCall_bubbles_and_breaksChain(@Mock Function<String, Collection<String>> sensor) {
        when(sensor.apply("2022")).thenThrow(new RuntimeException("SABOTAGE!"));
        lenient().when(sensor.apply("2023")).thenReturn(List.of("test2023_1", "test2023_2"));
        final var checkAfter = LocalDate.of(2022, Month.JANUARY, 1);
        final var reference = LocalDate.of(2023, Month.MARCH, 11);

        assertThatRuntimeException().isThrownBy(() -> YearAwareFetcher.combineResultsForEveryYear(checkAfter, reference, sensor)).withMessage("SABOTAGE!");

        verify(sensor).apply("2022");
        verify(sensor, never()).apply("2023");
    }
}

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

import at.oevsv.sota.data.domain.ActivatorLog;
import at.oevsv.sota.data.domain.ChaserLog;
import at.oevsv.sota.data.domain.SummitToSummitLog;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jetbrains.annotations.VisibleForTesting;

import javax.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

@ApplicationScoped
public class YearAwareFetcher {

    @Inject
    @RestClient
    ExternalDataService externalDataService;

    public Collection<ActivatorLog> fetchActivatorLogsById(String userId, @Nullable LocalDate checkAfter) {
        return combineResultsForEveryYear(checkAfter, year -> externalDataService.fetchActivatorLogsById(userId, year));
    }

    public Collection<ChaserLog> fetchChaserLogsById(String userId, @Nullable LocalDate checkAfter) {
        return combineResultsForEveryYear(checkAfter, year -> externalDataService.fetchChaserLogsById(userId, year));
    }

    public Collection<SummitToSummitLog> fetchSummitToSummitLogsById(String userId, @Nullable LocalDate checkAfter) {
        return combineResultsForEveryYear(checkAfter, year -> externalDataService.fetchSummitToSummitLogsById(userId, year));
    }

    private static <T> Collection<T> combineResultsForEveryYear(@Nullable LocalDate checkAfter, Function<String, Collection<T>> supplier) {
        return combineResultsForEveryYear(checkAfter, LocalDate.now(), supplier);
    }

    @VisibleForTesting
    static <T> Collection<T> combineResultsForEveryYear(@Nullable LocalDate checkAfter, LocalDate reference, Function<String, Collection<T>> supplier) {
        return combineAll(yearParametersFor(checkAfter, reference), supplier);
    }

    @VisibleForTesting
    static List<String> yearParametersFor(@Nullable LocalDate checkAfter, LocalDate reference) {
        if (checkAfter == null || checkAfter.isAfter(reference)) {
            return List.of("all");
        }

        final var spannedYears = Period.between(checkAfter, reference).getYears();
        // invariant: checkAfter is always before reference; thus, spannedYears >= 0
        if (spannedYears == 0) {
            return List.of(String.valueOf(checkAfter.getYear()));
        }
        if (spannedYears > 10) {
            return List.of("all");
        }

        // invariant: multiple years to cover
        return IntStream.rangeClosed(checkAfter.getYear(), reference.getYear())
                .mapToObj(String::valueOf)
                .toList();
    }

    @VisibleForTesting
    static <T> Collection<T> combineAll(List<String> parameters, Function<String, Collection<T>> supplier) {
        return parameters.stream()
                .map(supplier)
                .flatMap(Collection::stream)
                .toList();
    }
}

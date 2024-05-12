/*
 * Copyright (C) 2024 David Schwingenschlögl
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

import at.oevsv.sota.data.ExternalNewDataService;
import at.oevsv.sota.data.WireMockExtension;
import at.oevsv.sota.data.persistence.SummitList;
import at.oevsv.sota.data.persistence.SummitListTestSeam;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import org.awaitility.Awaitility;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@QuarkusTestResource(WireMockExtension.class)
final class StatisticResourceTest {

    @Inject
    StatisticResource statisticResource;

    @Inject
    SummitList summitList;

    @Inject
    @RestClient
    ExternalNewDataService externalNewDataService;

    @BeforeEach
    void synchronize() {
        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> SummitListTestSeam.isInitialSynchronizationCompleted(summitList));
    }

    /**
     * The prepared response in {@link WireMockExtension} answers with 5 QSOs for this day.
     */
    @Test
    @TestSecurity(user = "test", roles = "admin")
    void testCheckStatsForDayWith5QSOs() {
        final var testDate = LocalDate.of(2023, 4, 1);

        final var stats = statisticResource.checkStatsForDay(testDate);

        assertThat(stats).hasSameSizeAs(summitList.list());
        assertThat(stats.values())
                .as("Total sum of stats")
                .extracting(Integer::intValue)
                .satisfies(values -> {
                    long sum = values.stream().mapToInt(Integer::intValue).sum();
                    assertThat(sum).isEqualTo(19245L);
                });
    }

    /**
     * The prepared response in {@link WireMockExtension} answers with 10 QSOs for this day.
     */
    @Test
    @TestSecurity(user = "test", roles = "admin")
    void testCheckStatsForDayWith10QSOs() {
        final var testDate = LocalDate.of(2023, 4, 2);

        final var stats = statisticResource.checkStatsForDay(testDate);

        assertThat(stats).hasSameSizeAs(summitList.list());
        assertThat(stats.values())
                .as("Total sum of stats")
                .extracting(Integer::intValue)
                .satisfies(values -> {
                    long sum = values.stream().mapToInt(Integer::intValue).sum();
                    assertThat(sum).isEqualTo(38490L);
                });
    }
}

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
import at.oevsv.sota.data.domain.Summit;
import at.oevsv.sota.data.domain.SummitToSummitLog;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@QuarkusTestResource(WireMockExtension.class)
final class JacksonDeserializationTest {

    @RestClient
    @Inject
    ExternalDataService service;

    @Inject
    ObjectMapper objectMapper;

    @Test
    void deserializationTest() {
        assertThat(service.fetchActivatorLogsById("11330", "all")).extracting("summit").doesNotContainNull();
    }

    @Test
    void deserialize_activatorLog() throws JsonProcessingException {
        @Language("json")
        String json = """
                {
                  "0": 245247,
                  "OwnCallsign": "OE5JFE\\/P",
                  "1": "OE5JFE\\/P",
                  "Summit": "OE\\/OO-277 (Schieferstein)",
                  "2": "OE\\/OO-277 (Schieferstein)",
                  "ActivationDate": "2017-04-15",
                  "3": "2017-04-15 00:00:00",
                  "Points": 4,
                  "4": 4,
                  "BonusPoints": 0,
                  "5": 0,
                  "QSOs": 6,
                  "6": 6,
                  "QSO160": 0,
                  "7": 0,
                  "QSO80": 0,
                  "8": 0,
                  "QSO60": 0,
                  "9": 0,
                  "QSO40": 0,
                  "10": 0,
                  "QSO30": 0,
                  "11": 0,
                  "QSO20": 0,
                  "12": 0,
                  "QSO17": 0,
                  "13": 0,
                  "QSO15": 0,
                  "14": 0,
                  "QSO12": 0,
                  "15": 0,
                  "QSO10": 0,
                  "16": 0,
                  "QSO6": 0,
                  "17": 0,
                  "QSO4": 0,
                  "18": 0,
                  "QSO2": 6,
                  "19": 6,
                  "QSO70c": 0,
                  "20": 0,
                  "QSO23c": 0,
                  "21": 0,
                  "QSOssb": 0,
                  "22": 0,
                  "QSOcw": 0,
                  "23": 0,
                  "QSOfm": 6,
                  "24": 6,
                  "SummitID": 11927,
                  "25": 11927,
                  "Total": 4,
                  "26": 0
                }""";

        final var result = (ActivatorLog) objectMapper.reader().forType(ActivatorLog.class).readValue(json);

        assertThat(result.totalQSO()).isEqualTo(6);
        assertThat(result.summit()).isEqualTo(new Summit("OE/OO-277", "Schieferstein"));
        assertThat(result.activationDate()).isEqualTo(LocalDate.of(2017, 4, 15));
    }

    @Test
    void deserialize_chaserLog() throws JsonProcessingException {
        @Language("json")
        String json = """
                  {
                    "Confirmed": 0,
                    "ChaserLogID": 2773140,
                    "ActivationDate": "2017-04-30",
                    "OtherCallsign": "OE3GGS\\/P",
                    "Points": 2,
                    "SummitCode": "OE\\/OO-438",
                    "SummitName": "Ruine Ruttenstein",
                    "Total": 2
                  }
                """;
        final var result = (ChaserLog) objectMapper.reader().forType(ChaserLog.class).readValue(json);

        assertThat(result.chaserLogId()).isEqualTo("2773140");
        assertThat(result.summit()).isEqualTo(new Summit("OE/OO-438", "Ruine Ruttenstein"));
        assertThat(result.activationDate()).isEqualTo(LocalDate.of(2017, 4, 30));
    }

    @Test
    void deserialize_summitToSummitLog() throws JsonProcessingException {
        @Language("json")
        String json = """
                  {
                    "Confirmed": 0,
                    "ActivationDate": "2017-04-30",
                    "TimeOfDay": "10:47",
                    "OwnCallsign": "OE5JFE\\/P",
                    "OtherCallsign": "OE3GGS\\/P",
                    "SummitCode": "OE\\/OO-438",
                    "ChasedSummit": "Ruine Ruttenstein",
                    "lat1": 48.378201,
                    "lng1": 14.7711,
                    "Summit2Code": "OE\\/OO-073",
                    "ActivatedSummit": "Schoberstein",
                    "lat2": 47.905602,
                    "lng2": 14.3253,
                    "Band": "144MHz",
                    "Mode": "FM",
                    "Distance": 62,
                    "ChaserPoints": "2",
                    "ActivatorPoints": "4",
                    "Total": 6,
                    "Notes": ""
                  }
                """;
        final var result = (SummitToSummitLog) objectMapper.reader().forType(SummitToSummitLog.class).readValue(json);

        assertThat(result.ownCallSign()).isEqualTo("OE5JFE/P");
        assertThat(result.otherCallSign()).isEqualTo("OE3GGS/P");
        assertThat(result.activatedSummit()).isEqualTo(new Summit("OE/OO-073", "Schoberstein"));
        assertThat(result.chasedSummit()).isEqualTo(new Summit("OE/OO-438", "Ruine Ruttenstein"));
        assertThat(result.activationDate()).isEqualTo(LocalDate.of(2017, 4, 30));
    }
}

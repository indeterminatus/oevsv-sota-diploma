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

package at.oevsv.sota.data.persistence;

import at.oevsv.sota.data.domain.SummitListEntry;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

final class SummitListCsvParserTest {

    public static Stream<Arguments> samples() {
        return Stream.of(
                Arguments.of("OE/KT-176,Austria,Kärnten,Eckberg,1176,3858,13.4667,46.8333,13.46670,46.83330,4,0,01/04/2004,30/11/2016,5,29/07/2013,OE/OK1IPS/P", 1),
                Arguments.of("I/VE-283,Italy,Veneto,\"Monte Lozzo\",323,1060,11.6215,45.2954,11.62150,45.29540,1,0,01/10/2017,31/12/2099,6,18/11/2023,I1WKN/p", 0)
        );
    }

    @ParameterizedTest
    @MethodSource("samples")
    void parseCsvWorksForSampleInput(String csvLine, int expectedSize) {
        final var lines = List.of(
                "SOTA Summits List (Date=26/02/2024)",
                "SummitCode,AssociationName,RegionName,SummitName,AltM,AltFt,GridRef1,GridRef2,Longitude,Latitude,Points,BonusPoints,ValidFrom,ValidTo,ActivationCount,ActivationDate,ActivationCall",
                csvLine
        );
        Reader reader = new BufferedReader(new StringReader(String.join(System.lineSeparator(), lines)));

        List<SummitListEntry> result = SummitList.parseCsv(reader);

        assertThat(result).hasSize(expectedSize);
    }
}

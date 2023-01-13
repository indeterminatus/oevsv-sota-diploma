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

package at.oevsv.sota.data.domain.jackson;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

final class RegexSummitDeserializerTest {

    private static List<String> summits_valid() {
        return List.of("OE/OO-129 (Pfenningberg)", "OE/OO-330 (Mayrhofberg)", "OE/OO-354 (Hoher Trailing)", "OE/NO-003 (Ötscher)");
    }

    private static List<String> summits_invalid() {
        return List.of("", "IDT", "OE/OO-xxx", "OE/OO-129", "Pfennigberg");
    }

    @ParameterizedTest
    @MethodSource("summits_valid")
    void extractSummitFromString_positives(String check) {
        assertThat(RegexSummitDeserializer.extractSummitFromString(check)).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("summits_invalid")
    void extractSummitFromString_negatives(String check) {
        assertThat(RegexSummitDeserializer.extractSummitFromString(check)).isNull();
    }

    @Test
    void extractSummitFromString_isNullSafe() {
        assertThat(RegexSummitDeserializer.extractSummitFromString(null)).isNull();
    }
}

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

package at.oevsv.sota;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationUtilTest {

    private static List<String> callSigns_valid() {
        return List.of("OE5IDT", "OE5IDT/p", "oe5idt", "W2AEW", "OE20SOTA/P", "OE/OK2QA/P");
    }

    private static List<String> callSigns_invalid() {
        return List.of("", "IDT");
    }

    private static Stream<Arguments> callSign_pairs() {
        //@formatter:off
        return Stream.of(
            Arguments.of("OE5IDT", "OE5IDT/p", true),
            Arguments.of("OE5IDT", "OE5JFE", false),
            Arguments.of("oe5idt", "OE5IDT", true),
            Arguments.of("OE5IDT", "OE5", false),
            Arguments.of("OE5IDT", null, false),
            Arguments.of("OE20SOTA", "OE20SOTA/p", true),
            Arguments.of("OE20SOTA", "OE20SOTA/P", true),
            Arguments.of("oe20sota/p", "OE20SOTA/P", true),
            Arguments.of("HB9/OE5IDT/p", "oe5idt/p", true),
            Arguments.of(null, null, false),
            Arguments.of("OE5IDT", "", false),
            Arguments.of(null, "", false)
        );
        //@formatter:on
    }

    private static Stream<Arguments> callSign_identifiers() {
        //@formatter:off
        return Stream.of(
            Arguments.of("OE5IDT", "OE5IDT"),
            Arguments.of("OE5IDT/p", "OE5IDT"),
            Arguments.of("OE5IDT/am", "OE5IDT"),
            Arguments.of("DL/OE5IDT/am", "OE5IDT"),
            Arguments.of("HB9/OE5IDT/p", "OE5IDT")
        );
        //@formatter:on
    }

    @ParameterizedTest
    @MethodSource("callSigns_valid")
    void isCallSign_positives(String check) {
        assertThat(ValidationUtil.isCallSign(check)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("callSigns_invalid")
    void isCallSign_negatives(String check) {
        assertThat(ValidationUtil.isCallSign(check)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("callSign_pairs")
    void callSignsMatch(String left, String right, boolean expectedMatch) {
        assertThat(ValidationUtil.callSignsMatch(left, right)).as("normal order").isEqualTo(expectedMatch);
        assertThat(ValidationUtil.callSignsMatch(right, left)).as("reverse order").isEqualTo(expectedMatch);
    }

    @ParameterizedTest
    @MethodSource("callSign_identifiers")
    void callSignIdentifiersAreExtracted(String callSign, String identifier) {
        assertThat(ValidationUtil.extractIdentifier(callSign)).isEqualTo(identifier);
    }
}

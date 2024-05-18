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

package at.oevsv.sota.data.persistence;

import at.oevsv.sota.ValidationUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

final class DiplomaLogResourceTest {

    static Stream<Arguments> callSigns() {
        // @formatter:off
        return Stream.of(
                Arguments.of("OE5iDT", "oe5idt"),
                Arguments.of("oe5idt", "oe5idt"),
                Arguments.of("oe5idt/p", "oe5idt"),
                Arguments.of("oe5iDt/M", "oe5idt"),
                Arguments.of("oe5iDt/4", "oe5idt"),
                Arguments.of("dl/oe5iDt/4", "oe5idt")
        );
        // @formatter:on
    }

    @ParameterizedTest
    @MethodSource("callSigns")
    void canonicalCallSign_yieldsExpectedValue(String actual, String expected) {
        assertThat(DiplomaLogResource.canonicalCallSign(actual)).isEqualTo(expected);
    }

    @Test
    void canonicalCallSign_null_yieldsNull() {
        assertThat(DiplomaLogResource.canonicalCallSign(null)).isNull();
    }

    @Test
    void canonicalCallSign_invalid_yieldsNull() {
        assumeThat(ValidationUtil.isCallSign("oe5idt/")).isFalse();
        assertThat(DiplomaLogResource.canonicalCallSign("oe5idt/")).isNull();
    }

    @Test
    void canonicalCallSign_blank_yieldsNull() {
        assertThat(DiplomaLogResource.canonicalCallSign("")).isNull();
        assertThat(DiplomaLogResource.canonicalCallSign("   ")).isNull();
    }
}

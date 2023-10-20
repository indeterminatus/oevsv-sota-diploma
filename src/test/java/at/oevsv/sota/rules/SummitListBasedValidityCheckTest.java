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

import at.oevsv.sota.DiplomaResource;
import at.oevsv.sota.data.WireMockExtension;
import at.oevsv.sota.data.domain.Summit;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@QuarkusTestResource(WireMockExtension.class)
final class SummitListBasedValidityCheckTest {

    @Inject
    DiplomaResource sut;

    /**
     * It is known that OE/VB-357 is restricted to a specific date range.
     */
    @Test
    void oe_vb357_isRestrictedToDates() {
        final var map = sut.summitList();

        final var sut = new SummitListBasedValidityCheck(map);
        final var summit = new Summit("OE/VB-357", "Wannaköpfle");

        assertThat(sut.isValidAt(summit, LocalDate.of(2007, 7, 1), false)).isTrue();
        assertThat(sut.isValidAt(summit, LocalDate.of(2007, 7, 2), false)).isTrue();
        assertThat(sut.isValidAt(summit, LocalDate.of(2007, 6, 30), false)).isFalse();

        assertThat(sut.isValidAt(summit, LocalDate.of(2015, 12, 31), false)).isTrue();
        assertThat(sut.isValidAt(summit, LocalDate.of(2015, 12, 30), false)).isTrue();
        assertThat(sut.isValidAt(summit, LocalDate.of(2016, 1, 1), false)).isFalse();
    }

    @Test
    void unmanagedSummit_yieldsDefault() {
        final var map = sut.summitList();

        final var sut = new SummitListBasedValidityCheck(map);
        final var summit = new Summit("I/LO-422", "Monte Monarco");

        assertThat(sut.isValidAt(summit, LocalDate.of(2016, 1, 1), true)).isTrue();
        assertThat(sut.isValidAt(summit, LocalDate.of(2016, 1, 1), false)).isFalse();
    }
}

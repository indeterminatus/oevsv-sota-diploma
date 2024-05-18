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

package at.oevsv.sota.data.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validating for the rules; if two entries are equal, they only count as one occurrence; if they are not equal, they
 * count twice. We use a record for the discriminating properties.
 */
final class SpecialEntryOE20SOTATest {

    @Test
    void sameDateAndSameSummitIsEqual() {
        final var date = LocalDate.of(2024, 5, 1);
        final var summit = new Summit("OE/OO-001", "Does not matter");

        final var entry0 = new SpecialEntryOE20SOTA(date, summit);
        final var entry1 = new SpecialEntryOE20SOTA(date, summit);

        assertThat(entry0).isEqualTo(entry1).hasSameHashCodeAs(entry1);
    }

    @Test
    void differentDatesAndSameSummitIsNotEqual() {
        final var summit = new Summit("OE/OO-001", "Does not matter");

        final var entry0 = new SpecialEntryOE20SOTA(LocalDate.of(2024, 5, 1), summit);
        final var entry1 = new SpecialEntryOE20SOTA(LocalDate.of(2024, 5, 2), summit);

        assertThat(entry0).isNotEqualTo(entry1).doesNotHaveSameHashCodeAs(entry1);
    }

    @Test
    void sameDateAndDifferentSummitIsNotEqual() {
        final var date = LocalDate.of(2024, 5, 1);

        final var entry0 = new SpecialEntryOE20SOTA(date, new Summit("OE/OO-001", "Does not matter"));
        final var entry1 = new SpecialEntryOE20SOTA(date, new Summit("OE/OO-002", "Does not matter"));

        assertThat(entry0).isNotEqualTo(entry1).doesNotHaveSameHashCodeAs(entry1);
    }
}

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

package at.oevsv.sota.data.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Make sure the distinction between special diploma and "regular" ones can be made.
 */
final class CandidateTest {

    @Test
    void oe20sota_isSpecial() {
        assertThat(Candidate.Category.OE20SOTA).hasFieldOrPropertyWithValue("specialDiploma", true);
    }

    @ParameterizedTest
    @EnumSource(Candidate.Rank.class)
    void oe20sota_getsSameRequirementForAllRanks(Candidate.Rank rank) {
        assertThat(Candidate.Category.OE20SOTA.getRequirementFor(rank)).isEqualTo(20);
    }

    @ParameterizedTest
    @EnumSource(value = Candidate.Category.class, names = "OE20SOTA", mode = EnumSource.Mode.EXCLUDE)
    void allOtherCategories_areNotSpecial(Candidate.Category category) {
        assertThat(category).hasFieldOrPropertyWithValue("specialDiploma", false);
    }
}

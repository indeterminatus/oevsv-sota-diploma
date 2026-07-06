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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

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
	@EnumSource(value = Candidate.Category.class, names = {"OE20SOTA"}, mode = EnumSource.Mode.EXCLUDE)
	void allOtherCategories_areNotSpecial(Candidate.Category category) {
		assertThat(category).hasFieldOrPropertyWithValue("specialDiploma", false);
	}

	/**
	 * To ensure that an enum can be stored and loaded, keep the current DB limit in check!
	 */
	@ParameterizedTest
	@EnumSource(Candidate.Category.class)
	void categoryNamesAreShortEnoughForPersistence(Candidate.Category category) {
		// NB: I am not sure what the proper actual limit is in characters; the field is 15 long, maybe we need '\0' at
		// the end. Should this test fail, the decision what to do about it is deferred to that point.
		assertThat(category.name()).hasSizeLessThan(14);
	}
}

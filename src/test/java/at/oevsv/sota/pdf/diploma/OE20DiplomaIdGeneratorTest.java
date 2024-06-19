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

package at.oevsv.sota.pdf.diploma;

import at.oevsv.sota.data.api.Candidate;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

final class OE20DiplomaIdGeneratorTest {

    @Test
    void specialDiplomaOE20SOTAWorks() {
        final var special = new Candidate("XXX", null, Candidate.Category.OE20SOTA, Candidate.Rank.NONE, Map.of());
        final var sut = new OE20DiplomaIdGenerator(special, 1, "x");
        assertThat(sut.generateId()).isEqualTo("OE20-0001x");
    }
}

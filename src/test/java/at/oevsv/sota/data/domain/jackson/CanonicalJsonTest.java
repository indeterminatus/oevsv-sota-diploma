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

import at.oevsv.sota.data.api.Candidate;
import at.oevsv.sota.data.domain.Summit;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CanonicalJsonTest {

    @Test
    void null_yields_null() {
        assertThat(CanonicalJson.calculate(null)).isNull();
    }

    @Test
    void candidate_sortedAttributes() {
        final var input = new Candidate("OE5IDT", "123", Candidate.Category.CHASER, Candidate.Rank.NONE, Map.of(Summit.State.OE3, 4L, Summit.State.OE1, 1L));
        assertThat(CanonicalJson.calculate(input)).isEqualTo("{\"activations\":{\"OE1\":1,\"OE3\":4},\"callSign\":\"OE5IDT\",\"category\":\"CHASER\",\"rank\":\"NONE\",\"userID\":\"123\"}");
    }
}

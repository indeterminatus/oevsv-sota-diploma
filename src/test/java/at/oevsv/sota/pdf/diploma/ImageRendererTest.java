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

import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

final class ImageRendererTest {

    private static final Percentage PRECISION = Percentage.withPercentage(5.0);

    @Test
    void curvedPlacement() {
        assertThat(DefaultImageRenderer.StateLayoutMode.CURVED.heightFor(0, 0, 100)).as("start").isCloseTo(0, PRECISION);
        assertThat(DefaultImageRenderer.StateLayoutMode.CURVED.heightFor(4, 0, 100)).as("middle").isCloseTo(100, PRECISION);
        assertThat(DefaultImageRenderer.StateLayoutMode.CURVED.heightFor(8, 0, 100)).as("end").isCloseTo(0, PRECISION);
    }
}

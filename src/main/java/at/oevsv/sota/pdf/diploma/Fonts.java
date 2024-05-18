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

import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;

import java.io.IOException;

final class Fonts {

    private Fonts() {
        throw new AssertionError();
    }

    public static Font loadFont(String resourceName, int size, int style) throws IOException {
        final var base = BaseFont.createFont("/pdf/fonts/" + resourceName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        return new Font(base, size, style);
    }
}

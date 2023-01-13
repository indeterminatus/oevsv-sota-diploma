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

package at.oevsv.sota.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfGraphics2D;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

import java.io.IOException;

/**
 * PDF page event listener that adds the proper background image to the page once it's finished.
 *
 * @author schwingenschloegl
 */
final class PdfBackgroundSetter extends PdfPageEventHelper {

    private final float quality;
    private final ImageRenderer renderer;

    PdfBackgroundSetter(ImageRenderer renderer, float quality) {
        this.quality = quality;
        this.renderer = renderer;
    }

    @Override
    @SuppressWarnings("java:S112") // justification: No need for a specific exception
    public void onEndPage(PdfWriter writer, Document document) {
        try {
            final var pageSize = document.getPageSize();
            final var graphics2D = new PdfGraphics2D(writer.getDirectContentUnder(), pageSize.getWidth(), pageSize.getHeight(), null, false, true, quality);
            try {
                renderer.render(graphics2D);
            } finally {
                graphics2D.dispose();
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not set background image.", e);
        }
    }
}

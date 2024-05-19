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

import at.oevsv.sota.data.api.Generation;
import at.oevsv.sota.pdf.TextRenderer;
import com.lowagie.text.Chunk;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Objects;
import java.util.ResourceBundle;

final class OE20TextRenderer implements TextRenderer {

    private final Generation generation;
    private final String diplomaManager;
    private final boolean debugLayout;
    private final ResourceBundle resourceBundle;
    private final DiplomaIdGenerator idGenerator;

    public OE20TextRenderer(Generation generation, String diplomaManager, boolean debugLayout, DiplomaIdGenerator idGenerator) {
        this.generation = Objects.requireNonNull(generation);
        this.diplomaManager = Objects.requireNonNull(diplomaManager);
        this.debugLayout = debugLayout;
        this.resourceBundle = ResourceBundle.getBundle("pdf.i18n.messages", generation.getLocale(), ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_PROPERTIES));
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    @Override
    public void writeText(PdfWriter writer) throws IOException {
        final var consolas = Fonts.loadFont("consola.ttf", 11, Font.NORMAL);
        consolas.setColor(new Color(200, 200, 200));
        final var smaller = new Font(consolas);
        smaller.setSize(8);
        writeDiplomaManager(writer, smaller);

        final var diplomaId = idGenerator.generateId();
        writeDiplomaInfo(writer, smaller, LocalDate.now(), diplomaId);

        final var copperplateGothic = Fonts.loadFont("tiffanygtcc.ttf", 40, Font.BOLD);
        copperplateGothic.setColor(new Color(254, 253, 3));
        writeMainBox(writer, copperplateGothic);
    }

    private void writeDiplomaManager(PdfWriter writer, Font font) {
        final var paragraph = new Paragraph();
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.add(new Chunk(resourceBundle.getString("diploma.manager.label"), font));
        paragraph.add(Chunk.NEWLINE);
        paragraph.add(new Chunk(diplomaManager, font));

        final var bounds = writer.getPageSize();
        final var llx = bounds.getLeft(20.0f);
        // NB: 100.0f is the top line of the box; subtracting a bit because vertical alignment does not work properly
        final var lly = bounds.getBottom(100.0f - 20.0f);
        final var urx = bounds.getLeft(140.0f);
        final var ury = bounds.getBottom(25.0f);

        writeColumn(writer, paragraph, new Rectangle(llx, lly, urx, ury));
    }

    private void writeColumn(PdfWriter writer, Paragraph paragraph, Rectangle bounds) {
        ColumnText ct = new ColumnText(writer.getDirectContent());
        ct.setAlignment(Element.ALIGN_MIDDLE);
        ct.addElement(paragraph);

        final var llx = bounds.getLeft();
        final var lly = bounds.getBottom();
        final var urx = bounds.getRight();
        final var ury = bounds.getTop();
        ct.setSimpleColumn(llx, lly, urx, ury);
        ct.go();

        if (debugLayout) {
            debugRectangle(writer, llx, lly, urx, ury);
        }
    }

    private static void debugRectangle(PdfWriter writer, float llx, float lly, float urx, float ury) {
        final var cb = writer.getDirectContent();
        cb.setLineWidth(2.0f);
        cb.rectangle(llx, lly, urx - llx, ury - lly);
        cb.stroke();
    }

    private void writeDiplomaInfo(PdfWriter writer, Font font, LocalDate date, String diplomaId) {
        final var paragraph = new Paragraph();
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.add(new Chunk(resourceBundle.getString("diploma.id.label"), font));
        paragraph.add(new Chunk(" " + diplomaId, font));
        paragraph.add(Chunk.NEWLINE);
        paragraph.add(new Chunk(resourceBundle.getString("diploma.date.label"), font));
        final var formattedDate = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(generation.getLocale()).format(date);
        paragraph.add(new Chunk(" " + formattedDate, font));

        final var bounds = writer.getPageSize();
        final var llx = bounds.getRight(140.0f);
        // NB: 100.0f is the top line of the box; subtracting a bit because vertical alignment does not work properly
        final var lly = bounds.getBottom(100.0f - 20.0f);
        final var urx = bounds.getRight(20.0f);
        final var ury = bounds.getBottom(25.0f);

        writeColumn(writer, paragraph, new Rectangle(llx, lly, urx, ury));
    }

    private void writeMainBox(PdfWriter writer, Font highlightFont) {
        final var bounds = writer.getPageSize();
        final var llx = bounds.getLeft(220.0f);
        final var lly = bounds.getTop(170.0f + 4 * 25.0f);
        final var urx = bounds.getRight(220.0f);
        final var ury = bounds.getBottom(170.0f + 1.25f * 25.0f);

        final var name = new Paragraph();
        name.setAlignment(Element.ALIGN_CENTER);
        name.setMultipliedLeading(1.1f);
        name.add(new Chunk(generation.getRequester().callSign, highlightFont));
        name.add(Chunk.NEWLINE);
        name.add(Chunk.NEWLINE);
        final Font smaller = new Font(highlightFont);
        smaller.setSize(22);
        name.add(new Chunk(generation.getRequester().name, smaller));
        writeColumn(writer, name, new Rectangle(llx, lly, urx, ury));
    }
}

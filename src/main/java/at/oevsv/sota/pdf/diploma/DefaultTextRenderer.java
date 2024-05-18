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
import at.oevsv.sota.data.api.Requester;
import at.oevsv.sota.pdf.PdfGenerationResource;
import at.oevsv.sota.pdf.TextRenderer;
import com.ibm.icu.text.RuleBasedNumberFormat;
import com.lowagie.text.Chunk;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

final class DefaultTextRenderer implements TextRenderer {

    private final Candidate candidate;
    private final Requester requester;
    private final Locale locale;
    private final ResourceBundle resourceBundle;
    private final String diplomaManager;
    private final boolean debugLayout;
    private final DiplomaIdGenerator idGenerator;

    DefaultTextRenderer(PdfGenerationResource.Generation generation, String diplomaManager, boolean debugLayout, DiplomaIdGenerator idGenerator) {
        this.candidate = Objects.requireNonNull(generation.getCandidate());
        this.requester = Objects.requireNonNull(generation.getRequester());
        this.locale = Objects.requireNonNull(generation.getLocale());
        resourceBundle = ResourceBundle.getBundle("pdf.i18n.messages", locale, ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_PROPERTIES));
        this.diplomaManager = Objects.requireNonNull(diplomaManager);
        this.debugLayout = debugLayout;
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    @Override
    public void writeText(PdfWriter writer) throws IOException {
        final var britannic = Fonts.loadFont("britannic.ttf", 72, Font.BOLD);
        britannic.setColor(new Color(254, 253, 3));
        writeTitle(writer, britannic);

        final var consolas = Fonts.loadFont("consola.ttf", 11, Font.NORMAL);
        consolas.setColor(new Color(200, 200, 200));
        final var smaller = new Font(consolas);
        smaller.setSize(8);
        writeDiplomaManager(writer, smaller);

        final var diplomaId = idGenerator.generateId();
        writeDiplomaInfo(writer, smaller, LocalDate.now(), diplomaId);

        final var copperplateGothic = Fonts.loadFont("tiffanygtcc.ttf", 40, Font.BOLD);
        copperplateGothic.setColor(britannic.getColor());
        writeMainBox(writer, consolas, copperplateGothic);

        final var outline = Fonts.loadFont("florencesans.ttf", 26, Font.BOLD);
        writeRankBanner(writer, outline, consolas);
    }

    private void writeRankBanner(PdfWriter writer, Font bannerFont, Font surroundingFont) {
        final var paragraph = new Paragraph();
        paragraph.setAlignment(Element.ALIGN_CENTER);
        final var prefix = resourceBundle.getString("diploma.rank.prefix");
        if (StringUtils.isNotBlank(prefix)) {
            final var prefixChunk = new Chunk(prefix, surroundingFont);
            paragraph.add(prefixChunk);
        }
        final var chunk = new Chunk(resourceBundle.getString("diploma.rank." + candidate.rank().toString().toLowerCase(Locale.ROOT)), bannerFont);
        chunk.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_STROKE, 0.1f, Color.BLACK);
        paragraph.add(chunk);
        final var suffix = resourceBundle.getString("diploma.rank.suffix");
        if (StringUtils.isNotBlank(suffix)) {
            final var suffixChunk = new Chunk(suffix, surroundingFont);
            paragraph.add(suffixChunk);
        }

        final var bounds = writer.getPageSize();
        final var llx = bounds.getLeft(205.0f);
        final var lly = bounds.getTop(130.0f);
        final var urx = bounds.getRight(205.0f);
        final var ury = bounds.getBottom(180.0f);
        final var height = ury - lly;

        writeColumn(writer, paragraph, new Rectangle(llx, lly + height / 3.0f + 35.0f, urx, ury));
    }

    private void writeDiplomaManager(PdfWriter writer, Font font) {
        final var paragraph = new Paragraph();
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.add(new Chunk(resourceBundle.getString("diploma.manager.label"), font));
        paragraph.add(Chunk.NEWLINE);
        paragraph.add(new Chunk(diplomaManager, font));

        final var bounds = writer.getPageSize();
        final var llx = bounds.getLeft(25.0f);
        // NB: 100.0f is the top line of the box; subtracting a bit because vertical alignment does not work properly
        final var lly = bounds.getBottom(100.0f - 20.0f);
        final var urx = bounds.getLeft(145.0f);
        final var ury = bounds.getBottom(30.0f);

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
        final var formattedDate = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale).format(date);
        paragraph.add(new Chunk(" " + formattedDate, font));

        final var bounds = writer.getPageSize();
        final var llx = bounds.getRight(145.0f);
        // NB: 100.0f is the top line of the box; subtracting a bit because vertical alignment does not work properly
        final var lly = bounds.getBottom(100.0f - 20.0f);
        final var urx = bounds.getRight(25.0f);
        final var ury = bounds.getBottom(30.0f);

        writeColumn(writer, paragraph, new Rectangle(llx, lly, urx, ury));
    }

    private void writeTitle(PdfWriter writer, Font font) {
        final var paragraph = new Paragraph();
        paragraph.setAlignment(Element.ALIGN_CENTER);
        final var title = new Chunk(resourceBundle.getString("diploma.title"), font);
        title.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE, 3.0f, Color.BLACK);
        paragraph.add(title);

        final var bounds = writer.getPageSize();
        final var llx = bounds.getLeft();
        final var lly = bounds.getTop(80.0f);
        final var urx = bounds.getRight();
        final var ury = bounds.getTop(110.0f);

        writeColumn(writer, paragraph, new Rectangle(llx, lly, urx, ury));
    }

    private void writeMainBox(PdfWriter writer, Font normalFont, Font highlightFont) {
        final var bounds = writer.getPageSize();
        final var llx = bounds.getLeft(205.0f);
        final var lly = bounds.getTop(130.0f);
        final var urx = bounds.getRight(205.0f);
        final var ury = bounds.getBottom(190.0f);
        final var height = ury - lly;

        final var header = new Paragraph();
        header.setAlignment(Element.ALIGN_CENTER);
        header.add(new Chunk(resourceBundle.getString("diploma.header"), normalFont));
        header.add(Chunk.NEWLINE);
        header.add(new Chunk(resourceBundle.getString("diploma.category." + candidate.category().name().toLowerCase(Locale.ROOT)), normalFont));
        writeColumn(writer, header, new Rectangle(llx, lly, urx, lly + height / 3.0f));

        final var name = new Paragraph();
        name.setAlignment(Element.ALIGN_CENTER);
        name.add(new Chunk(requester.callSign, highlightFont));
        name.add(Chunk.NEWLINE);
        name.add(Chunk.NEWLINE);
        final Font smaller = new Font(highlightFont);
        smaller.setSize(22);
        name.add(new Chunk(requester.name, smaller));
        writeColumn(writer, name, new Rectangle(llx, lly + height / 3.0f - 30.0f, urx, lly + 2.0f * height / 3.0f - 30.0f));

        final var bottom = new Paragraph();
        bottom.setAlignment(Element.ALIGN_CENTER);
        final String body = formatDiplomaBody();
        for (final var chunk : body.split("\n")) {
            bottom.add(new Chunk(chunk, normalFont));
            bottom.add(Chunk.NEWLINE);
        }
        bottom.add(new Chunk(resourceBundle.getString("diploma.footer"), normalFont));

        writeColumn(writer, bottom, new Rectangle(llx, lly + 2.0f * height / 3.0f - 30.0f, urx, ury));
    }

    @Nonnull
    private String formatDiplomaBody() {
        final var activations = formatNumeric(candidate.category().getRequirementFor(candidate.rank()));
        final var states = formatNumeric(candidate.rank().getRequiredStates());
        return MessageFormat.format(
                resourceBundle.getString("diploma.body." + candidate.category().name().toLowerCase(Locale.ROOT)), activations, states);
    }

    @Nonnull
    private String formatNumeric(int number) {
        final var format = new RuleBasedNumberFormat(locale, RuleBasedNumberFormat.SPELLOUT);
        return MessageFormat.format("{0} ({1})", number, format.format(number));
    }
}

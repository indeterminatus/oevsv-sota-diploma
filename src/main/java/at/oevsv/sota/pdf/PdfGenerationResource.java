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

import at.oevsv.sota.data.api.Generation;
import at.oevsv.sota.pdf.diploma.DiplomaFormats;
import com.google.common.annotations.VisibleForTesting;
import com.lowagie.text.Document;
import com.lowagie.text.Rectangle;
import com.lowagie.text.RectangleReadOnly;
import com.lowagie.text.pdf.PdfWriter;
import io.quarkus.logging.Log;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

@Path("/api/diploma/pdf")
public class PdfGenerationResource {

    private static final int EXPECTED_SIZE = 4 * 1024 * 1024;

    private final DiplomaFormats diplomaFormats;

    @Inject
    public PdfGenerationResource(DiplomaFormats diplomaFormats) {
        this.diplomaFormats = Objects.requireNonNull(diplomaFormats);
    }

    @POST
    @Path("/generate")
    @RolesAllowed("admin")
    @Blocking
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/pdf")
    public Response generatePdf(Generation generation) throws IOException {
        String fileName = fileNameFor(generation);
        byte[] bytes = generatePdfBytes(generation, fileName);

        return Response.ok(bytes, "application/pdf")
                .header("Content-Disposition", "attachment;filename=" + fileName)
                .build();
    }

    public String fileNameFor(Generation generation) {
        DiplomaGenerator format = diplomaFormats.generationStrategyFor(generation);
        if (format != null) {
            return format.fileNameFor(generation);
        }

        throw new IllegalStateException(String.format("No format found for generation %s", generation));
    }

    public byte[] generatePdfBytes(Generation generation, String fileName) throws IOException {
        return generateBinaryAsBytes(generation, fileName);
    }

    @Nonnull
    private byte[] generateBinaryAsBytes(Generation generation, String fileName) throws IOException {
        Log.infof("Generating diploma %s", fileName);
        DiplomaGenerator format = diplomaFormats.generationStrategyFor(generation);
        if (format != null) {
            byte[] bytes = generateBinary(generation, format);
            Log.infof("Generated diploma %s (%d bytes)", fileName, bytes.length);

            return bytes;
        } else {
            throw new IOException(String.format("No format found for generation %s", generation));
        }
    }

    @VisibleForTesting
    static byte[] generateBinary(Generation generation, DiplomaGenerator format) throws IOException {
        try (final var os = new ByteArrayOutputStream(EXPECTED_SIZE)) {
            try (final Document document = new Document()) {
                document.setDocumentLanguage(generation.getLocale().getLanguage());

                final Rectangle pageSize = highResA4Landscape();
                document.setPageSize(pageSize);
                document.setMargins(0, 0, 0, 0);
                document.setPageCount(1);
                final var writer = PdfWriter.getInstance(document, os);
                writer.setPageEvent(new PdfBackgroundSetter(format.createImageRenderer(generation), generation.getQuality()));
                document.open();

                final var textRenderer = format.createTextRenderer(generation);
                textRenderer.writeText(writer);
            }

            // NB: the document must be closed so the stream is properly finalized
            return os.toByteArray();
        }
    }

    /**
     * Takes care of the fact that OpenPDF expects the page size in millimeters in 1/72 inches.
     *
     * @return a {@link Rectangle} with the proper dimensions
     */
    @Nonnull
    private static Rectangle highResA4Landscape() {
        float width = PdfUtils.millimetersToUserSpace(297.0f);
        float height = PdfUtils.millimetersToUserSpace(210.0f);
        return new RectangleReadOnly(width, height);
    }
}

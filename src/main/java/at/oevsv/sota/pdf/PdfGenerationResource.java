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

import at.oevsv.sota.data.api.Candidate;
import at.oevsv.sota.data.api.Requester;
import at.oevsv.sota.data.domain.jackson.StringToLocaleConverter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.annotations.VisibleForTesting;
import com.lowagie.text.Document;
import com.lowagie.text.Rectangle;
import com.lowagie.text.RectangleReadOnly;
import com.lowagie.text.pdf.PdfWriter;
import io.quarkus.logging.Log;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

@ApplicationScoped
@Path("/api/diploma/pdf")
public class PdfGenerationResource {

    private static final int EXPECTED_SIZE = 4 * 1024 * 1024;

    @ConfigProperty(name = "diploma.manager", defaultValue = "Martin Reiter, OE5REO")
    String diplomaManager;

    @ConfigProperty(name = "diploma.debug.layout", defaultValue = "false")
    boolean debugLayout;

    @RegisterForReflection
    public static final class Generation {

        @JsonProperty
        private Requester requester;

        @JsonProperty
        private Candidate candidate;

        @JsonProperty
        private int sequence;

        @JsonProperty
        @Nullable
        private String sequenceSuffix;

        @JsonProperty
        @JsonDeserialize(converter = StringToLocaleConverter.class)
        @DefaultValue("de-AT")
        private Locale locale = Locale.GERMAN;

        @JsonProperty
        @DefaultValue("0.95")
        private float quality = 0.95f;

        @SuppressWarnings("unused")
        public Generation() {
            // Required for bean contract.
        }

        public Generation(Requester requester, Candidate candidate) {
            this.requester = requester;
            this.candidate = candidate;
        }

        public Locale getLocale() {
            return locale;
        }

        public void setLocale(Locale locale) {
            this.locale = locale;
        }

        public void setQuality(float quality) {
            this.quality = quality;
        }

        public void setSequence(int sequence) {
            this.sequence = sequence;
        }

        public void setSequenceSuffix(@Nullable String sequenceSuffix) {
            this.sequenceSuffix = sequenceSuffix;
        }
    }

    @POST
    @Path("/generate")
    @RolesAllowed("admin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response generatePdf(Generation generation) throws IOException {
        String fileName = fileNameFor(generation);
        byte[] bytes = generatePdfBytes(generation, fileName);

        return Response.ok(bytes, MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment;filename=" + fileName)
                .build();
    }

    public String fileNameFor(Generation generation) {
        return fileNameFor(generation.requester, generation.candidate);
    }

    public byte[] generatePdfBytes(Generation generation) throws IOException {
        return generatePdfBytes(generation, fileNameFor(generation));
    }

    @Nonnull
    private byte[] generatePdfBytes(Generation generation, String fileName) throws IOException {
        Log.infof("Generating diploma %s", fileName);
        byte[] bytes = generateBinary(generation.requester, generation.candidate, generation.sequence, generation.sequenceSuffix, generation.quality, generation.locale, diplomaManager, debugLayout);
        Log.infof("Generated diploma %s (%d bytes)", fileName, bytes.length);

        return bytes;
    }

    @VisibleForTesting
    static byte[] generateBinary(Requester requester, Candidate candidate, int sequence, @Nullable String sequenceSuffix, float quality, Locale locale, String diplomaManager, boolean debugLayout) throws IOException {
        try (final var os = new ByteArrayOutputStream(EXPECTED_SIZE)) {
            try (final Document document = new Document()) {
                document.setDocumentLanguage(locale.getLanguage());

                final Rectangle pageSize = highResA4Landscape();
                document.setPageSize(pageSize);
                document.setMargins(0, 0, 0, 0);
                document.setPageCount(1);
                final var writer = PdfWriter.getInstance(document, os);
                writer.setPageEvent(new PdfBackgroundSetter(new ImageRenderer(candidate, debugLayout), quality));
                document.open();

                final var textRenderer = new TextRenderer(candidate, locale, requester, diplomaManager, sequence, sequenceSuffix, debugLayout);
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

    @VisibleForTesting
    static String fileNameFor(Requester requester, Candidate candidate) {
        final StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.toRootLowerCase(requester.callSign));
        sb.append('_');
        final var category = candidate.category().toString().substring(0, 3);
        sb.append(StringUtils.toRootLowerCase(category));
        sb.append('_');
        final var rank = candidate.rank().toString();
        sb.append(StringUtils.toRootLowerCase(rank));
        sb.append(".pdf");
        return sb.toString();
    }
}

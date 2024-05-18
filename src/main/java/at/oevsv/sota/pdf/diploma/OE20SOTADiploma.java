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
import at.oevsv.sota.pdf.DiplomaGenerator;
import at.oevsv.sota.pdf.ImageRenderer;
import at.oevsv.sota.pdf.PdfGenerationResource;
import at.oevsv.sota.pdf.TextRenderer;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

/**
 * Generating special OE20SOTA diploma.
 */
@ApplicationScoped
final class OE20SOTADiploma implements DiplomaGenerator {

    @Override
    public boolean canHandle(PdfGenerationResource.Generation generation) {
        return generation.getCandidate().category() == Candidate.Category.OE20SOTA;
    }

    @Override
    public String fileNameFor(PdfGenerationResource.Generation generation) {
        final StringBuilder sb = new StringBuilder();
        sb.append("oe20_");
        if (generation.getSequence() > 0) {
            final var id = String.format(Locale.ROOT, "%04d", generation.getSequence());
            sb.append(StringUtils.appendIfMissing(id, generation.getSequenceSuffix()));
            sb.append('_');
        }
        sb.append(StringUtils.toRootLowerCase(generation.getRequester().callSign));
        sb.append(".pdf");
        return sb.toString();
    }

    @Override
    public String idFor(PdfGenerationResource.Generation generation) {
        return new DefaultDiplomaIdGenerator(generation.getCandidate(), generation.getSequence(), generation.getSequenceSuffix()).generateId();
    }

    @Override
    public ImageRenderer createImageRenderer(PdfGenerationResource.Generation generation) {
        return null;
    }

    @Override
    public TextRenderer createTextRenderer(PdfGenerationResource.Generation generation) {
        return null;
    }
}

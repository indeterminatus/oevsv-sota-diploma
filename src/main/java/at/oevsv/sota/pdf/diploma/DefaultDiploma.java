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

import at.oevsv.sota.pdf.DiplomaGenerator;
import at.oevsv.sota.data.api.Generation;
import at.oevsv.sota.pdf.ImageRenderer;
import at.oevsv.sota.pdf.TextRenderer;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Generating the original diploma.
 */
@ApplicationScoped
final class DefaultDiploma implements DiplomaGenerator {

    @ConfigProperty(name = "diploma.manager", defaultValue = "Martin Reiter, OE5REO")
    String diplomaManager;

    @ConfigProperty(name = "diploma.debug.layout", defaultValue = "false")
    boolean debugLayout;

    @Override
    public boolean canHandle(Generation generation) {
        return !generation.getCandidate().category().isSpecialDiploma();
    }

    @Override
    public String fileNameFor(Generation generation) {
        final StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.toRootLowerCase(generation.getRequester().callSign));
        sb.append('_');
        final var category = generation.getCandidate().category().toString().substring(0, 3);
        sb.append(StringUtils.toRootLowerCase(category));
        sb.append('_');
        final var rank = generation.getCandidate().rank().toString();
        sb.append(StringUtils.toRootLowerCase(rank));
        sb.append(".pdf");
        return sb.toString();
    }

    @Override
    public String idFor(Generation generation) {
        return new DefaultDiplomaIdGenerator(generation.getCandidate(), generation.getSequence(), generation.getSequenceSuffix()).generateId();
    }

    @Override
    public ImageRenderer createImageRenderer(Generation generation) {
        return new DefaultImageRenderer(generation.getCandidate(), debugLayout);
    }

    @Override
    public TextRenderer createTextRenderer(Generation generation) {
        return new DefaultTextRenderer(generation, diplomaManager, debugLayout, () -> idFor(generation));
    }
}

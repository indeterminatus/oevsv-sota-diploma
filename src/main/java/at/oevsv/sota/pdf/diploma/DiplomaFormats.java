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
import at.oevsv.sota.pdf.PdfGenerationResource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public final class DiplomaFormats {

    private final List<DiplomaGenerator> strategies;

    @Inject
    public DiplomaFormats(Instance<DiplomaGenerator> strategies) {
        this.strategies = new ArrayList<>();
        strategies.stream().forEachOrdered(this.strategies::add);
    }

    @Nullable
    public DiplomaGenerator generationStrategyFor(PdfGenerationResource.Generation generationType) {
        for (final var strategy : strategies) {
            if (strategy.canHandle(generationType)) {
                return strategy;
            }
        }

        return null;
    }
}

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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Locale;

final class OE20DiplomaIdGenerator extends AbstractDiplomaIdGenerator {

    public OE20DiplomaIdGenerator(Candidate candidate, @Min(0L) @Max(99_999L) int sequence, @Nullable String sequenceSuffix) {
        super(candidate, sequence, sequenceSuffix);
    }

    @Override
    public String generateId() {
        final var id = String.format(Locale.ROOT, "OE20-%04d", sequence);
        return StringUtils.appendIfMissing(id, sequenceSuffix);
    }
}

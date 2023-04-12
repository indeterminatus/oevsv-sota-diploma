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

package at.oevsv.sota.rules;

import at.oevsv.sota.data.domain.Summit;
import at.oevsv.sota.data.domain.SummitListEntry;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.Map;

record SummitListBasedValidityCheck(Map<String, SummitListEntry> summitsByCode) implements SummitValidityCheck {

    @Override
    public boolean isValidAt(@Nullable Summit summit, @Nullable LocalDate instant, boolean defaultIfUnknown) {
        if (summit == null || instant == null) {
            return defaultIfUnknown;
        }

        final var listEntry = summitsByCode.get(summit.code());
        if (listEntry != null) {
            final var afterStart = instant.isEqual(listEntry.getValidFrom()) || instant.isAfter(listEntry.getValidFrom());
            final var beforeEnd = instant.isEqual(listEntry.getValidTo()) || instant.isBefore(listEntry.getValidTo());
            return afterStart && beforeEnd;
        }

        return defaultIfUnknown;
    }
}

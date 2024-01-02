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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Objects;

/**
 * Generate a stable ID from provided data.
 *
 * @author schwingenschloegl
 */
final class DefaultDiplomaIdGenerator implements DiplomaIdGenerator {

    private final Candidate candidate;
    private final int sequence;
    private final String sequenceSuffix;

    public DefaultDiplomaIdGenerator(Candidate candidate, @Min(0L) @Max(99_999L) int sequence) {
        this(candidate, sequence, null);
    }

    @SuppressWarnings("ConstantValue") // justification: not trusting validation enough
    public DefaultDiplomaIdGenerator(Candidate candidate, @Min(0L) @Max(99_999L) int sequence, @Nullable String sequenceSuffix) {
        this.candidate = Objects.requireNonNull(candidate);
        if (sequence < 0) {
            throw new IllegalArgumentException(String.format("sequence must not be negative! Passed: %d", sequence));
        }
        if (sequence > 99_999) {
            throw new IllegalArgumentException(String.format("sequence must not exceed 99_999! Passed: %d", sequence));
        }
        // invariant: 0 <= sequence <= 99_999
        this.sequence = sequence;

        if (sequenceSuffix != null && sequenceSuffix.length() > 1) {
            throw new IllegalArgumentException(String.format("sequenceSuffix must not exceed one character! Passed: %s", sequenceSuffix));
        }
        // invariant: |sequenceSuffix| <= 1

        if (sequenceSuffix != null && sequenceSuffix.matches("\\d")) {
            throw new IllegalArgumentException(String.format("sequenceSuffix must not contain digits! Passed: %s", sequenceSuffix));
        }
        this.sequenceSuffix = sequenceSuffix;
    }

    @Override
    public String generateId() {
        final var category = candidate.category().toString().substring(0, 3).toUpperCase(Locale.ROOT);
        final var rank = candidate.rank().toString().substring(0, 2).toUpperCase(Locale.ROOT);

        final var id = String.format(Locale.ROOT, "%s-%s-%04d", category, rank, sequence);
        return StringUtils.appendIfMissing(id, sequenceSuffix);
    }
}

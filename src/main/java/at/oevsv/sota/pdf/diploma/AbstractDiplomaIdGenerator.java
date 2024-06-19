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

import javax.annotation.Nullable;
import java.util.Objects;

abstract class AbstractDiplomaIdGenerator implements DiplomaIdGenerator {

    protected final Candidate candidate;
    protected final int sequence;
    protected final String sequenceSuffix;

    @SuppressWarnings("ConstantValue") // justification: not trusting validation enough
    protected AbstractDiplomaIdGenerator(Candidate candidate, @Min(0L) @Max(99_999L) int sequence, @Nullable String sequenceSuffix) {
        this.candidate = Objects.requireNonNull(candidate);
        this.sequence = sequence;
        this.sequenceSuffix = sequenceSuffix;

        if (sequence < 0) {
            throw new IllegalArgumentException(String.format("sequence must not be negative! Passed: %d", sequence));
        }
        if (sequence > 99_999) {
            throw new IllegalArgumentException(String.format("sequence must not exceed 99_999! Passed: %d", sequence));
        }
        // invariant: 0 <= sequence <= 99_999

        if (sequenceSuffix != null && sequenceSuffix.length() > 1) {
            throw new IllegalArgumentException(String.format("sequenceSuffix must not exceed one character! Passed: %s", sequenceSuffix));
        }
        // invariant: |sequenceSuffix| <= 1

        if (sequenceSuffix != null && sequenceSuffix.matches("\\d")) {
            throw new IllegalArgumentException(String.format("sequenceSuffix must not contain digits! Passed: %s", sequenceSuffix));
        }
    }
}

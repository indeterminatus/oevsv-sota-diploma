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

package at.oevsv.sota.data.domain;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.intellij.lang.annotations.Language;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.regex.Pattern;

@RegisterForReflection
public record Summit(String code, String name) {

    public enum State {
        OE1("^OE/WI-\\d{3}$"),
        OE2("^OE/SB-\\d{3}$"),
        OE3("^OE/NO-\\d{3}$"),
        OE4("^OE/BL-\\d{3}$"),
        OE5("^OE/OO-\\d{3}$"),
        OE6("^OE/ST-\\d{3}$"),
        OE7("^OE/T[IL]-\\d{3}$"),
        OE8("^OE/KT-\\d{3}$"),
        OE9("^OE/VB-\\d{3}$");

        private final Pattern pattern;

        State(@Nonnull @Language("RegExp") String regex) {
            this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        }

        @Nullable
        public static State stateForSummitCode(@Nonnull String summitCode) {
            for (State probe : values()) {
                if (probe.pattern.matcher(summitCode).matches()) {
                    return probe;
                }
            }

            // invariant: if control reaches this point, no state matches the summit
            return null;
        }

        /**
         * Convenience method to return the matching {@link State} to a state's ordinal value; the mapping is:
         * <ul>
         *     <li>1 -> OE1</li>
         *     <li>2 -> OE2</li>
         *     <li>3 -> OE3</li>
         *     <li>...</li>
         *     <li>9 -> OE9</li>
         * </ul>
         *
         * @param ordinal ordinal value of state: <code>[1..9]</code>
         * @return a {@link State} or null if the ordinal passed is out of range
         */
        @Nullable
        public static State stateForOrdinal(int ordinal) {
            final int index = ordinal - 1;
            if (index >= 0 && index < values().length) {
                return values()[index];
            }

            // invariant: ordinal is out of bounds
            return null;
        }
    }

    public State state() {
        return State.stateForSummitCode(code());
    }
}

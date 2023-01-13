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

package at.oevsv.sota;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.regex.Pattern;

/**
 * Collection of validation utility methods.
 *
 * @author schwingenschloegl
 */
final class ValidationUtil {

    private static final Pattern CALL_SIGN_PATTERN = Pattern.compile("^([A-Z0-9]+/)?[A-Z0-9]{1,2}\\d+[A-Z]+(/[A-Z0-9]+)?$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private ValidationUtil() {
        throw new AssertionError();
    }

    /**
     * Returns true iff the passed parameter to check is a syntactically valid call sign.
     *
     * @param check the argument to check
     * @return true iff a syntactically valid call sign is passed
     */
    public static boolean isCallSign(@Nullable String check) {
        if (check == null || check.isBlank()) {
            return false;
        }

        return CALL_SIGN_PATTERN.matcher(check).matches();
    }

    /**
     * Returns true iff the passed arguments are logically the same call sign. This method should take care of filtering
     * out suffixes. At least one of the arguments passed must satisfy {@link #isCallSign(String)}.
     *
     * @param left  left operand
     * @param right right operand
     * @return if the call signs match
     */
    public static boolean callSignsMatch(String left, String right) {
        if (!isCallSign(left) || !isCallSign(right)) {
            return false;
        }

        if (StringUtils.equalsIgnoreCase(left, right)) {
            return true;
        }

        return StringUtils.startsWithIgnoreCase(right, left) || StringUtils.startsWithIgnoreCase(left, right);
    }
}

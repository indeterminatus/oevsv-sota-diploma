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

/**
 * Collection of utility methods to facilitate PDF generation.
 *
 * @author schwingenschloegl
 */
final class PdfUtils {

    private PdfUtils() {
        throw new AssertionError();
    }

    /**
     * Transforms millimeter to the intrinsic PDF user unit.
     *
     * @param mm scalar in millimeters
     * @return scalar in PDF user space units
     */
    public static float millimetersToUserSpace(float mm) {
        return mm * 72.0f / 25.4f;
    }
}

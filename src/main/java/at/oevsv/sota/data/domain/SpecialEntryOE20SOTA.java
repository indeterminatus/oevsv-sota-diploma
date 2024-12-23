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

package at.oevsv.sota.data.domain;

import javax.annotation.Nullable;
import java.time.LocalDate;

/**
 * The primary intent is to determine the uniqueness-trait of chaser log entries with respect to the rules of
 * the special diploma SpecialEntryOE20SOTA.
 *
 * @param activationDate
 * @param summit
 */
public record SpecialEntryOE20SOTA(LocalDate activationDate, @Nullable Summit summit) {
}

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

import at.oevsv.sota.data.domain.jackson.RegexSummitDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalDate;

public record ActivatorLog(
        @JsonProperty(value = "Summit", required = true)
        @JsonDeserialize(using = RegexSummitDeserializer.class) Summit summit,
        @JsonProperty("QSOs") int totalQSO,
        @JsonProperty("Points") int points,
        @JsonProperty("ActivationDate") LocalDate activationDate) {
}

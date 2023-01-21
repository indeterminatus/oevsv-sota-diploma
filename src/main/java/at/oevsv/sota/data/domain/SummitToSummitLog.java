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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * A single log entry of a summit-to-summit event.
 *
 * <p>
 * Note that Jackson cannot "virtually" assemble two JSON fields to a single {@link Summit} instance
 * without a big hassle that would produce a lot of boilerplate code. Therefore, properties that begin with an underscore
 * are meant as internal members that should not be used directly. Use the {@link #chasedSummit()} and {@link #activatedSummit()} methods instead!
 * This is not ideal, but since the application is so small and the effects localized, I made a tradeoff in design here.
 * </p>
 *
 * @param ownCallSign      own call sign
 * @param otherCallSign    call sign of other end of QSO
 * @param activationDate   date of activation
 * @param _summit2Code     code of activated summit; do not use directly
 * @param _activatedSummit name of activated summit; do not use directly
 * @param _summitCode      code of chased summit; do not use directly
 * @param _chasedSummit    name of chased summit; do not use directly
 */
public record SummitToSummitLog(@JsonProperty("OwnCallsign") String ownCallSign,
                                @JsonProperty("OtherCallsign") String otherCallSign,
                                @JsonProperty("ActivationDate") LocalDate activationDate,
                                @JsonProperty("Summit2Code") String _summit2Code,
                                @JsonProperty("ActivatedSummit") String _activatedSummit,
                                @JsonProperty("SummitCode") String _summitCode,
                                @JsonProperty("ChasedSummit") String _chasedSummit) {

    public Summit chasedSummit() {
        return new Summit(_summitCode, _chasedSummit);
    }

    public Summit activatedSummit() {
        return new Summit(_summit2Code, _activatedSummit);
    }
}

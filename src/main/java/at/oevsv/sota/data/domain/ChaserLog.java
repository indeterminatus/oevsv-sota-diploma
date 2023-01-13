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
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.time.LocalDate;

/**
 * A single log entry of a chaser.
 *
 * <p>
 * Note that Jackson cannot "virtually" assemble two JSON fields to a single {@link Summit} instance
 * without a big hassle that would produce a lot of boilerplate code. Therefore, properties that begin with an underscore
 * are meant as internal members that should not be used directly. Use the {@link #summit()} method instead!
 * This is not ideal, but since the application is so small and the effects localized, I made a tradeoff in design here.
 * </p>
 *
 * @param chaserLogId
 * @param callSign
 * @param _summitCode do not use this directly; required only for proper deserialization
 * @param _summitName do not use this directly; required only for proper deserialization
 */
public record ChaserLog(@JsonProperty("ChaserLogID") String chaserLogId, @JsonProperty("OtherCallsign") String callSign,
                        @JsonProperty("ActivationDate") LocalDate activationDate,
                        @JsonProperty("SummitCode") String _summitCode,
                        @JsonProperty("SummitName") String _summitName) {

    @Nullable
    public Summit summit() {
        if (StringUtils.isNotBlank(_summitCode) && StringUtils.isNotBlank(_summitName)) {
            return new Summit(_summitCode, _summitName);
        }

        return null;
    }
}

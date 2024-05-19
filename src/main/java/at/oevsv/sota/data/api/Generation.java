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

package at.oevsv.sota.data.api;

import at.oevsv.sota.data.domain.jackson.StringToLocaleConverter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.quarkus.runtime.annotations.RegisterForReflection;

import javax.annotation.Nullable;
import java.util.Locale;

@RegisterForReflection
public class Generation {

    @JsonProperty
    private Requester requester;

    @JsonProperty
    private Candidate candidate;

    @JsonProperty
    private int sequence;

    @JsonProperty
    @Nullable
    private String sequenceSuffix;

    @JsonProperty(defaultValue = "de-AT")
    @JsonDeserialize(converter = StringToLocaleConverter.class)
    private Locale locale = Locale.GERMAN;

    @JsonProperty(defaultValue = "0.95")
    private float quality = 0.95f;

    @SuppressWarnings("unused")
    public Generation() {
        // Required for bean contract.
    }

    public Generation(Requester requester, Candidate candidate) {
        this.requester = requester;
        this.candidate = candidate;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void setQuality(float quality) {
        this.quality = quality;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public void setSequenceSuffix(@Nullable String sequenceSuffix) {
        this.sequenceSuffix = sequenceSuffix;
    }

    public Requester getRequester() {
        return requester;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public int getSequence() {
        return sequence;
    }

    @Nullable
    public String getSequenceSuffix() {
        return sequenceSuffix;
    }

    public void setRequester(Requester requester) {
        this.requester = requester;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public float getQuality() {
        return quality;
    }
}

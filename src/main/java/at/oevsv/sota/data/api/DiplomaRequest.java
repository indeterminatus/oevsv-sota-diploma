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

package at.oevsv.sota.data.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;

/**
 * Contains a specific request for a diploma, containing everything required to create and deliver. This is merely a container to collect the required data structures into one.
 *
 * @param requester
 * @param candidates
 * @author schwingenschloegl
 */
public record DiplomaRequest(@JsonProperty("requester") Requester requester,
                             @JsonProperty("candidates") Collection<SignedCandidate> candidates) {
}

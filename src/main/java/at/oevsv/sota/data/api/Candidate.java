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

import at.oevsv.sota.data.domain.Summit;

import java.util.Map;

/**
 * A candidate contains technical, condensed data to determine rank, category, ... for a specific user.
 *
 * @param callSign    canonical call-sign of the user
 * @param userID      internal ID of the user for faster lookup
 * @param category    for which the diploma should be requested
 * @param rank        the rank according to the rules
 * @param activations sum of events per state
 * @author schwingenschloegl
 */
public record Candidate(String callSign, String userID, Category category, Rank rank,
                        Map<Summit.State, Long> activations) {

    public enum Category {
        ACTIVATOR(40, 20, 10),
        CHASER(40, 20, 10),
        S2S(40, 20, 10),
        /**
         * Special occasion: from 2024-05-01 to 2024-10-31 there is OE20SOTA/P; chasers working that
         * call-sign are eligible for this diploma.
         */
        OE20SOTA(20, 20, 20);

        private final int goldRequirement;
        private final int silverRequirement;
        private final int bronzeRequirement;

        Category(int goldRequirement, int silverRequirement, int bronzeRequirement) {
            this.goldRequirement = goldRequirement;
            this.silverRequirement = silverRequirement;
            this.bronzeRequirement = bronzeRequirement;
        }

        public int getGoldRequirement() {
            return goldRequirement;
        }

        public int getSilverRequirement() {
            return silverRequirement;
        }

        public int getBronzeRequirement() {
            return bronzeRequirement;
        }

        public boolean isSpecialDiploma() {
            return goldRequirement == silverRequirement && silverRequirement == bronzeRequirement;
        }

        public int getRequirementFor(Rank rank) {
            if (isSpecialDiploma()) {
                return goldRequirement; // all choices are equal
            }

            return switch (rank) {
                case GOLD -> getGoldRequirement();
                case SILVER -> getSilverRequirement();
                case BRONZE -> getBronzeRequirement();
                case NONE -> 0;
            };
        }
    }

    public enum Rank {
        GOLD(6),
        SILVER(4),
        BRONZE(2),
        NONE(0);

        private final int requiredStates;

        Rank(int requiredStates) {
            this.requiredStates = requiredStates;
        }

        public int getRequiredStates() {
            return requiredStates;
        }
    }
}

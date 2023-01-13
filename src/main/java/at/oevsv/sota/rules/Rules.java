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

package at.oevsv.sota.rules;

import at.oevsv.sota.data.api.Candidate;
import at.oevsv.sota.data.domain.ActivatorLog;
import at.oevsv.sota.data.domain.ChaserLog;
import at.oevsv.sota.data.domain.Summit;
import at.oevsv.sota.data.domain.SummitListEntry;
import at.oevsv.sota.data.domain.SummitToSummitLog;
import org.jetbrains.annotations.VisibleForTesting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

/**
 * Central class that manages all rule evaluations.
 *
 * @author schwingenschloegl
 */
public final class Rules {

    private Rules() {
        throw new AssertionError();
    }

    /**
     * To make argument lists shorter.
     *
     * @param callSign
     * @param userId
     * @param summitList
     * @param checkOnlyAfter
     */
    public record CommonArguments(String callSign, String userId, Map<String, SummitListEntry> summitList,
                                  LocalDate checkOnlyAfter) {
    }

    @Nonnull
    public static Candidate determineDiplomaCandidateAsActivator(Collection<ActivatorLog> activatorLogs, CommonArguments common) {
        final SummitValidityCheck summitCheck = new SummitListBasedValidityCheck(common.summitList());
        final Map<Summit.State, Long> frequencies =
                activatorLogs.stream()
                        .filter(log -> log.points() > 0)
                        .filter(log -> isWithinTimeRange(log.activationDate(), common))
                        .filter(log -> summitCheck.isValidAt(log.summit(), log.activationDate()))
                        .map(ActivatorLog::summit)
                        .filter(Objects::nonNull)
                        .filter(summit -> summit.state() != null)
                        .collect(groupingBy(Summit::state, () -> new EnumMap<>(Summit.State.class), counting()));

        return createCandidate(common.callSign(), common.userId(), frequencies, Candidate.Category.ACTIVATOR);
    }

    @Nonnull
    private static Candidate createCandidate(String callSign, String userId, Map<Summit.State, Long> frequencies, Candidate.Category category) {
        final int differentStates = frequencies.size();
        final long activations = frequencies.values().stream().reduce(0L, Long::sum);

        for (final var rank : Candidate.Rank.values()) {
            if (differentStates >= rank.getRequiredStates() && activations >= category.getRequirementFor(rank)) {
                return new Candidate(callSign, userId, category, rank, frequencies);
            }
        }

        return new Candidate(callSign, userId, category, Candidate.Rank.NONE, frequencies);
    }

    @Nonnull
    public static Candidate determineDiplomaCandidateAsChaser(Collection<ChaserLog> chaserLogs, CommonArguments common) {
        final SummitValidityCheck summitCheck = new SummitListBasedValidityCheck(common.summitList());
        final Map<Summit.State, Long> frequencies =
                chaserLogs.stream()
                        .filter(log -> isWithinTimeRange(log.activationDate(), common))
                        .filter(log -> summitCheck.isValidAt(log.summit(), log.activationDate()))
                        .map(ChaserLog::summit)
                        .filter(Objects::nonNull)
                        .filter(summit -> summit.state() != null)
                        .collect(groupingBy(Summit::state, () -> new EnumMap<>(Summit.State.class), counting()));

        return createCandidate(common.callSign(), common.userId(), frequencies, Candidate.Category.CHASER);
    }

    @Nonnull
    public static Candidate determineDiplomaCandidateForSummitToSummit(Collection<SummitToSummitLog> summitToSummitLogs, CommonArguments common) {
        final SummitValidityCheck summitCheck = new SummitListBasedValidityCheck(common.summitList());
        final Map<Summit.State, Long> frequencies =
                summitToSummitLogs.stream()
                        .filter(log -> isWithinTimeRange(log.activationDate(), common))
                        .filter(log -> summitCheck.isValidAt(log.activatedSummit(), log.activationDate()))
                        .filter(log -> summitCheck.isValidAt(log.chasedSummit(), log.activationDate()))
                        .map(Rules::pickSummitForState)
                        .filter(Objects::nonNull)
                        .collect(groupingBy(Summit::state, () -> new EnumMap<>(Summit.State.class), counting()));

        return createCandidate(common.callSign(), common.userId(), frequencies, Candidate.Category.S2S);
    }

    @VisibleForTesting
    static boolean isWithinTimeRange(LocalDate date, CommonArguments common) {
        final var restriction = common.checkOnlyAfter;
        return restriction == null || date.isAfter(restriction) || date.isEqual(restriction);
    }

    @Nullable
    private static Summit pickSummitForState(SummitToSummitLog log) {
        final var chasedSummit = log.chasedSummit();
        if (chasedSummit.state() != null) {
            return chasedSummit;
        }

        return null;
    }
}

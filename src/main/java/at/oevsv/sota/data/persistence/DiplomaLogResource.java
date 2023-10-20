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

package at.oevsv.sota.data.persistence;

import at.oevsv.sota.data.api.Candidate;
import at.oevsv.sota.data.api.DiplomaRequest;
import at.oevsv.sota.data.api.Requester;
import at.oevsv.sota.data.api.SignedCandidate;
import at.oevsv.sota.data.domain.Summit;
import io.quarkus.panache.common.Parameters;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.VisibleForTesting;

import javax.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.IntSupplier;

@Path("/api/logs")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public final class DiplomaLogResource {

    @POST
    @PermitAll
    @Transactional
    public boolean create(DiplomaRequest request) {
        final var signedCandidates = request.candidates();
        if (signedCandidates == null || signedCandidates.isEmpty()) {
            return false;
        }

        signedCandidates.forEach(SignedCandidate::verifyIntegrity);

        // invariant: there is at least 1 candidate; all of them are valid
        int totalCreated = 0;
        final var candidates = signedCandidates.stream().map(SignedCandidate::candidate).toList();
        for (final var candidate : candidates) {
            if (!alreadyRequested(request.requester(), candidate)) {
                createEntryInDatabase(request, candidate);
                ++totalCreated;
            }
        }

        return totalCreated > 0;
    }

    private static void createEntryInDatabase(DiplomaRequest request, Candidate candidate) {
        final var entry = new DiplomaLog();
        entry.setMail(request.requester().mail);
        entry.setName(request.requester().name);
        entry.setCallSign(canonicalCallSign(request.requester().callSign));
        entry.setRank(candidate.rank());
        entry.setCreationDate(LocalDate.now());
        entry.setCategory(candidate.category());
        entry.setReviewMailSent(false);

        entry.setActivationsOE1(candidate.activations().getOrDefault(Summit.State.OE1, 0L).intValue());
        entry.setActivationsOE2(candidate.activations().getOrDefault(Summit.State.OE2, 0L).intValue());
        entry.setActivationsOE3(candidate.activations().getOrDefault(Summit.State.OE3, 0L).intValue());
        entry.setActivationsOE4(candidate.activations().getOrDefault(Summit.State.OE4, 0L).intValue());
        entry.setActivationsOE5(candidate.activations().getOrDefault(Summit.State.OE5, 0L).intValue());
        entry.setActivationsOE6(candidate.activations().getOrDefault(Summit.State.OE6, 0L).intValue());
        entry.setActivationsOE7(candidate.activations().getOrDefault(Summit.State.OE7, 0L).intValue());
        entry.setActivationsOE8(candidate.activations().getOrDefault(Summit.State.OE8, 0L).intValue());
        entry.setActivationsOE9(candidate.activations().getOrDefault(Summit.State.OE9, 0L).intValue());
        if (StringUtils.equalsIgnoreCase(request.language(), "en")) {
            entry.setLanguage(request.language());
        }
        entry.persist();
    }

    @VisibleForTesting
    @Nullable
    static String canonicalCallSign(@Nullable String callSign) {
        if (StringUtils.trimToNull(callSign) == null) {
            return null;
        }

        final var lowered = StringUtils.toRootLowerCase(callSign);
        return StringUtils.substringBefore(lowered, '/');
    }

    @GET
    @RolesAllowed("admin")
    @Path("/pending")
    @Transactional
    public List<DiplomaLog> listPending() {
        return listPendingInternal();
    }

    @Transactional
    public List<DiplomaLog> listPendingInternal() {
        return DiplomaLog.list("select s from DiplomaLog s where s.reviewMailSent = false");
    }

    @PUT
    @RolesAllowed("admin")
    @Path("/{id}")
    @Transactional
    public DiplomaLog update(@PathParam("id") Long id, DiplomaLog entry) {
        final DiplomaLog entity = DiplomaLog.findById(id);
        if (entity == null) {
            throw new NotFoundException();
        }

        entity.setReviewMailSent(entry.isReviewMailSent());
        return entity;
    }

    @Transactional
    public void markReviewMailSent(Long id) {
        final DiplomaLog entity = DiplomaLog.findById(id);
        if (entity != null) {
            entity.setReviewMailSent(true);
        }
    }

    @Transactional
    @VisibleForTesting
    void deleteAll() {
        DiplomaLog.deleteAll();
    }

    @Transactional
    public boolean alreadyRequested(Requester requester, Candidate candidate) {
        final var parameters = Parameters.with("callSign", canonicalCallSign(requester.callSign)).and("category", candidate.category()).and("rank", candidate.rank());
        return DiplomaLog.count("from DiplomaLog s where s.callSign=:callSign and s.category=:category and s.rank=:rank", parameters) >= 1L;
    }

    public static Requester toRequester(DiplomaLog diplomaLog) {
        return new Requester(diplomaLog.getCallSign(), diplomaLog.getMail(), diplomaLog.getName());
    }

    public static Candidate toCandidate(DiplomaLog diplomaLog) {
        final EnumMap<Summit.State, Long> activations = new EnumMap<>(Summit.State.class);
        checkAndAdd(diplomaLog::getActivationsOE1, Summit.State.OE1, activations);
        checkAndAdd(diplomaLog::getActivationsOE2, Summit.State.OE2, activations);
        checkAndAdd(diplomaLog::getActivationsOE3, Summit.State.OE3, activations);
        checkAndAdd(diplomaLog::getActivationsOE4, Summit.State.OE4, activations);
        checkAndAdd(diplomaLog::getActivationsOE5, Summit.State.OE5, activations);
        checkAndAdd(diplomaLog::getActivationsOE6, Summit.State.OE6, activations);
        checkAndAdd(diplomaLog::getActivationsOE7, Summit.State.OE7, activations);
        checkAndAdd(diplomaLog::getActivationsOE8, Summit.State.OE8, activations);
        checkAndAdd(diplomaLog::getActivationsOE9, Summit.State.OE9, activations);
        return new Candidate(diplomaLog.getCallSign(), null, diplomaLog.getCategory(), diplomaLog.getRank(), activations);
    }

    public static Locale toRequestedLocale(DiplomaLog diplomaLog) {
        return StringUtils.equalsIgnoreCase(diplomaLog.getLanguage(), "en") ? Locale.ENGLISH : Locale.GERMAN;
    }

    private static void checkAndAdd(IntSupplier check, Summit.State state, Map<Summit.State, Long> target) {
        final var value = check.getAsInt();
        if (value > 0) {
            target.put(state, (long) value);
        }
    }
}

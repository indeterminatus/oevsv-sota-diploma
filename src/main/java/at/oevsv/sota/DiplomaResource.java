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

package at.oevsv.sota;

import at.oevsv.sota.data.ExternalDataService;
import at.oevsv.sota.data.api.Candidate;
import at.oevsv.sota.data.api.DiplomaRequest;
import at.oevsv.sota.data.api.Requester;
import at.oevsv.sota.data.api.SignedCandidate;
import at.oevsv.sota.data.domain.Activator;
import at.oevsv.sota.data.domain.Chaser;
import at.oevsv.sota.data.domain.ShortWaveListener;
import at.oevsv.sota.data.domain.SummitListEntry;
import at.oevsv.sota.data.persistence.DiplomaLogResource;
import at.oevsv.sota.data.persistence.SummitList;
import at.oevsv.sota.rules.Rules;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.cache.CacheResult;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.mutiny.redis.client.RedisAPI;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static at.oevsv.sota.ValidationUtil.callSignsMatch;

@Path("/api/diploma")
public class DiplomaResource {

    @Inject
    @RestClient
    ExternalDataService externalDataService;

    @Inject
    SummitList summitsService;

    @Inject
    DiplomaLogResource logs;

    @Inject
    RedisAPI redis;

    @ConfigProperty(name = "requests.per.minute", defaultValue = "5")
    int requestsPerMinute;

    @ConfigProperty(name = "check.after.date", defaultValue = "2023-01-01")
    LocalDate checkAfter;

    Collection<Activator> fetchActivators() {
        return externalDataService.fetchActivators("0");
    }

    Collection<Chaser> fetchChasers() {
        return externalDataService.fetchChasers("0");
    }

    Collection<ShortWaveListener> fetchShortWaveListeners() {
        return externalDataService.fetchShortWaveListeners("0");
    }

    @WithSpan(kind = SpanKind.SERVER, value = "Fetch complete summit list")
    public Map<String, SummitListEntry> summitList() {
        return summitsService.list().stream().collect(Collectors.toMap(SummitListEntry::getSummitCode, x -> x));
    }

    @CacheResult(cacheName = "userid-cache")
    @Nullable
    @WithSpan(kind = SpanKind.SERVER, value = "Lookup UserID")
    public String userIdForCallSign(@SpanAttribute("callSign") @Nullable String callSign) {
        if (!ValidationUtil.isCallSign(callSign)) {
            return null;
        }

        // invariant: callSign is syntactically correct
        final var activatorId = fetchActivators().stream()
                .filter(activator -> callSignsMatch(callSign, activator.callSign()))
                .findAny()
                .map(Activator::userId);
        if (activatorId.isPresent()) {
            return activatorId.get();
        }

        final var chaserId = fetchChasers().stream()
                .filter(chaser -> callSignsMatch(callSign, chaser.callSign()))
                .findAny()
                .map(Chaser::userId);
        // TODO: check SWL
        return chaserId.orElse(null);
    }

    @GET
    @PermitAll
    @Path("/candidates")
    @Produces("application/json")
    @Blocking
    @WithSpan(kind = SpanKind.SERVER, value = "Check Diploma")
    public Collection<SignedCandidate> checkCandidatesForUser(@SpanAttribute("callSign") @QueryParam("callsign") String callSign, @Context HttpServerRequest request) {
        enforceRateLimit(request);

        final var userId = userIdForCallSign(callSign);
        if (userId == null) {
            throw new NotFoundException("No user found for callsign.");
        }

        final var summitList = summitList();
        final var common = new Rules.CommonArguments(callSign, userId, summitList, checkAfter);

        final Collection<Candidate> result = new ArrayList<>();
        final var activatorCandidate = Rules.determineDiplomaCandidateAsActivator(externalDataService.fetchActivatorLogsById(userId, "all"), common);
        result.add(activatorCandidate);

        final var chaserCandidate = Rules.determineDiplomaCandidateAsChaser(externalDataService.fetchChaserLogsById(userId, "all"), common);
        result.add(chaserCandidate);

        final var summitToSummitCandidate = Rules.determineDiplomaCandidateForSummitToSummit(externalDataService.fetchSummitToSummitLogsById(userId, "all"), common);
        result.add(summitToSummitCandidate);

        final var requester = new Requester(callSign, null, null);
        return result.stream()
                .filter(candidate -> !logs.alreadyRequested(requester, candidate))
                .map(SignedCandidate::sign)
                .toList();
    }

    /**
     * This is a stateful operation that enforces a server-side rate limit. If control returns normally, the request rate is in lieu with the limits. If a threshold is violated, a {@link WebApplicationException} is thrown.
     *
     * @param request the request context if available
     * @throws WebApplicationException if the rate limit is exceeded
     * @implNote since the bucket used does not depend on the URL be careful where this method is invoked!
     */
    private void enforceRateLimit(@Nullable HttpServerRequest request) {
        final var now = LocalDateTime.now();
        final String throttlingKey = getThrottlingKey(request) + ":" + now.getMinute();
        final var requests = await(redis.get(throttlingKey));
        final int requestNumber = (requests != null) ? requests.toInteger() : 0;
        if (requestNumber >= requestsPerMinute) {
            throw new WebApplicationException(Response.status(Response.Status.TOO_MANY_REQUESTS).header("X-Rate-Limit-Retry-After-Seconds", 60 - now.getSecond()).build());
        }

        await(redis.multi());
        try {
            await(redis.incr(throttlingKey));
            await(redis.expire(List.of(throttlingKey, "60")));
        } finally {
            await(redis.exec());
        }
    }

    /**
     * Determines the key relevant to throttling; for now, we try to determine the caller's IP, and fall back to a universal value if that fails.
     * This is not the most solid approach and might lock out NATed users, but quite frankly this ought to be good enough. If there are many
     * complaints that the service appears to be down, we can refine here.
     *
     * @param request the request context if available
     * @return a non-null key to use as "bucket" for server-side rate-limiting
     */
    @Nonnull
    private static String getThrottlingKey(@Nullable HttpServerRequest request) {
        if (request != null) {
            final var ip = request.remoteAddress().host();
            if (StringUtils.isNotBlank(ip)) {
                return ip;
            }
        }
        return "global";
    }

    @Nullable
    private io.vertx.redis.client.Response await(Uni<io.vertx.mutiny.redis.client.Response> mutinyResponse) {
        final var response = mutinyResponse.await().atMost(Duration.ofSeconds(20L));
        if (response == null) {
            return null;
        }
        return response.getDelegate();
    }

    @POST
    @PermitAll
    @Path("/request")
    @Produces("application/json")
    @Consumes("application/json")
    @Blocking
    @WithSpan(kind = SpanKind.SERVER, value = "Request Diploma")
    public boolean requestDiploma(@SpanAttribute("callSign") @QueryParam("callsign") String callSign, DiplomaRequest request) {
        return logs.create(request);
    }
}

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
import at.oevsv.sota.data.YearAwareFetcher;
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
import io.quarkus.logging.Log;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.mutiny.redis.client.RedisAPI;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

    @ConfigProperty(name = "requests.per.minute", defaultValue = "5")
    int requestsPerMinute;

    @ConfigProperty(name = "check.after.date", defaultValue = "2023-01-01")
    LocalDate checkAfter;

    private final ExternalDataService externalDataService;
    private final YearAwareFetcher yearAwareFetcher;
    private final SummitList summitsService;
    private final DiplomaLogResource logs;
    private final RedisAPI redis;

    @Inject
    public DiplomaResource(@RestClient ExternalDataService externalDataService, YearAwareFetcher yearAwareFetcher, SummitList summitsService, DiplomaLogResource logs, RedisAPI redis) {
        this.externalDataService = externalDataService;
        this.yearAwareFetcher = yearAwareFetcher;
        this.summitsService = summitsService;
        this.logs = logs;
        this.redis = redis;
    }

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

        // NB: we could check SWL here, but it was decided that we do not need to support them.
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

        Log.infof("Retrieving candidates for user %s since %s", callSign, checkAfter);
        final var userId = userIdForCallSign(callSign);
        if (userId == null) {
            throw new NotFoundException("No user found for callsign.");
        }

        final var summitList = summitList();
        final var common = new Rules.CommonArguments(callSign, userId, summitList, checkAfter);

        final Collection<Candidate> result = new ArrayList<>();
        result.add(Rules.determineDiplomaCandidateAsActivator(yearAwareFetcher.fetchActivatorLogsById(userId, checkAfter), common));

        final var chaserLogs = yearAwareFetcher.fetchChaserLogsById(userId, checkAfter);
        result.add(Rules.determineDiplomaCandidateAsChaser(chaserLogs, common));
        result.add(Rules.determineDiplomaCandidateForSummitToSummit(yearAwareFetcher.fetchSummitToSummitLogsById(userId, checkAfter), common));
        result.add(Rules.determineDiplomaCandidateForSpecialOE20SOTA(chaserLogs, common));

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
        final var throttlingKey = getThrottlingKey(request) + ":" + now.getMinute();
        final var requests = await(redis.get(throttlingKey));
        final int requestNumber = (requests != null) ? requests.toInteger() : 0;
        if (requestNumber >= requestsPerMinute) {
            Log.warnf("Exceeded %d requests per minute; key: %s", requestsPerMinute, throttlingKey);
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

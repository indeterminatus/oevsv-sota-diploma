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

import at.oevsv.sota.data.ExternalNewDataService;
import at.oevsv.sota.data.domain.SummitActivationLog;
import at.oevsv.sota.data.persistence.SummitList;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.cache.CacheResult;
import io.quarkus.logging.Log;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Path("/api/statistic")
public class StatisticResource {

    private final ExternalNewDataService externalNewDataService;
    private final SummitList summitsService;

    @Inject
    public StatisticResource(@RestClient ExternalNewDataService externalNewDataService, SummitList summitsService) {
        this.externalNewDataService = externalNewDataService;
        this.summitsService = summitsService;
    }

    @GET
    @RolesAllowed("admin")
    @Path("/day/{day}")
    @Produces("application/json")
    @Blocking
    @WithSpan(kind = SpanKind.SERVER, value = "Check stats for day")
    public Map<String, Integer> checkStatsForDay(@PathParam("day") LocalDate date) {
        final var summits = summitsService.list();
        final Map<String, Collection<SummitActivationLog>> activationsPerSummit = new HashMap<>();
        for (final var summit : summits) {
            final var activationLogs = summitActivationLogs(summit.getSummitCode());
            if (activationLogs != null && !activationLogs.isEmpty()) {
                activationsPerSummit.put(summit.getSummitCode(), activationLogs);
            }
        }

        final Map<String, Integer> qsoPerSummit = new HashMap<>();
        for (final var entry : activationsPerSummit.entrySet()) {
            final var activationLogs = entry.getValue();
            final var totalQso = activationLogs.stream().filter(log -> log.activationDate().isEqual(date)).mapToInt(SummitActivationLog::totalQSO).sum();
            if (totalQso > 0) {
                qsoPerSummit.put(entry.getKey(), totalQso);
            }
        }

        return qsoPerSummit;
    }

    @CacheResult(cacheName = "summit-activation-cache")
    @Nullable
    @WithSpan(kind = SpanKind.SERVER, value = "Lookup summit activations")
    public Collection<SummitActivationLog> summitActivationLogs(String summitCode) {
        try {
            return externalNewDataService.fetchActivationsForSummit(summitCode);
        } catch (WebApplicationException e) {
            if (e.getResponse().getStatus() != Response.Status.NOT_FOUND.getStatusCode()) {
                Log.error("Error fetching activations for summit: " + summitCode, e);
            }
            return null;
        } catch (Exception e) {
            Log.error("Error fetching activations for summit: " + summitCode, e);
            return null;
        }
    }
}

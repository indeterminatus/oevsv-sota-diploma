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

package at.oevsv.sota.data;

import at.oevsv.sota.data.domain.Activator;
import at.oevsv.sota.data.domain.ActivatorLog;
import at.oevsv.sota.data.domain.Chaser;
import at.oevsv.sota.data.domain.ChaserLog;
import at.oevsv.sota.data.domain.ShortWaveListener;
import at.oevsv.sota.data.domain.SummitToSummitLog;
import io.quarkus.cache.CacheResult;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.annotations.GZIP;

import jakarta.inject.Singleton;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import java.time.temporal.ChronoUnit;
import java.util.Collection;

@Singleton
@Path("/admin")
@RegisterRestClient(configKey = "api-db")
public interface ExternalDataService {

    @CacheResult(cacheName = "activator-cache")
    @GET
    @GZIP
    @Path("/activator_roll")
    @Produces("application/json")
    Collection<Activator> fetchActivators(@DefaultValue("0") @QueryParam("associationID") String associationId);

    @CacheResult(cacheName = "chaser-cache")
    @GET
    @GZIP
    @Path("/chaser_roll")
    @Produces("application/json")
    Collection<Chaser> fetchChasers(@DefaultValue("0") @QueryParam("associationID") String associationId);

    @CacheResult(cacheName = "swl-cache")
    @GET
    @GZIP
    @Path("/swl_roll")
    @Produces("application/json")
    Collection<ShortWaveListener> fetchShortWaveListeners(@DefaultValue("0") @QueryParam("associationID") String associationId);

    @GET
    @GZIP
    @Path("/activator_log_by_id")
    @Produces("application/json")
    @Bulkhead(value = 3)
    @CircuitBreaker(requestVolumeThreshold = 4, successThreshold = 10, delay = 10L, delayUnit = ChronoUnit.SECONDS)
    Collection<ActivatorLog> fetchActivatorLogsById(@QueryParam("id") String userId, @QueryParam("year") @DefaultValue("all") String year);

    @GET
    @GZIP
    @Path("/chaser_log_by_id")
    @Produces("application/json")
    @Bulkhead(value = 3)
    @CircuitBreaker(requestVolumeThreshold = 4, successThreshold = 10, delay = 10L, delayUnit = ChronoUnit.SECONDS)
    Collection<ChaserLog> fetchChaserLogsById(@QueryParam("id") String userId, @QueryParam("year") @DefaultValue("all") String year);

    @GET
    @GZIP
    @Path("/s2s_log_by_id")
    @Produces("application/json")
    @Bulkhead(value = 3)
    @CircuitBreaker(requestVolumeThreshold = 4, successThreshold = 10, delay = 10L, delayUnit = ChronoUnit.SECONDS)
    Collection<SummitToSummitLog> fetchSummitToSummitLogsById(@QueryParam("id") String userId, @QueryParam("year") @DefaultValue("all") String year);
}

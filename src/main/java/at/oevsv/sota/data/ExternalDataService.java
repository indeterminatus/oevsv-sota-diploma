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

import java.time.temporal.ChronoUnit;
import java.util.Collection;

import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import at.oevsv.sota.data.domain.Activator;
import at.oevsv.sota.data.domain.ActivatorLog;
import at.oevsv.sota.data.domain.Chaser;
import at.oevsv.sota.data.domain.ChaserLog;
import at.oevsv.sota.data.domain.ShortWaveListener;
import at.oevsv.sota.data.domain.SummitToSummitLog;
import io.quarkus.cache.CacheResult;
import io.quarkus.vertx.http.Compressed;
import jakarta.inject.Singleton;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;

@Singleton
@Path("/")
@RegisterRestClient(configKey = "api-db2")
public interface ExternalDataService {

	@CacheResult(cacheName = "activator-cache")
	@GET
	@Compressed
	@Path("/rolls/activator/{associationID}/{year}/all/all")
	@Produces("application/json")
	Collection<Activator> fetchActivators(@DefaultValue("-1") @PathParam("associationID") String associationId,
			@DefaultValue("0") @PathParam("year") int year);

	@CacheResult(cacheName = "chaser-cache")
	@GET
	@Compressed
	@Path("/rolls/activator/{associationID}/{year}/all/all")
	@Produces("application/json")
	Collection<Chaser> fetchChasers(@DefaultValue("-1") @PathParam("associationID") String associationId,
			@DefaultValue("0") @PathParam("year") int year);

	@CacheResult(cacheName = "swl-cache")
	@GET
	@Compressed
	@Path("/rolls/swl/{associationID}/{year}/all/all")
	@Produces("application/json")
	Collection<ShortWaveListener> fetchShortWaveListeners(
			@DefaultValue("-1") @PathParam("associationID") String associationId,
			@DefaultValue("0") @PathParam("year") int year);

	@GET
	@Compressed
	@Path("/admin/activator_log_by_id")
	@Produces("application/json")
	@Bulkhead(value = 3)
	@CircuitBreaker(requestVolumeThreshold = 4, successThreshold = 10, delay = 10L, delayUnit = ChronoUnit.SECONDS)
	Collection<ActivatorLog> fetchActivatorLogsById(@QueryParam("id") String userId,
			@QueryParam("year") @DefaultValue("all") String year);

	@GET
	@Compressed
	@Path("/admin/chaser_log_by_id")
	@Produces("application/json")
	@Bulkhead(value = 3)
	@CircuitBreaker(requestVolumeThreshold = 4, successThreshold = 10, delay = 10L, delayUnit = ChronoUnit.SECONDS)
	Collection<ChaserLog> fetchChaserLogsById(@QueryParam("id") String userId,
			@QueryParam("year") @DefaultValue("all") String year);

	@GET
	@Compressed
	@Path("/admin/s2s_log_by_id")
	@Produces("application/json")
	@Bulkhead(value = 3)
	@CircuitBreaker(requestVolumeThreshold = 4, successThreshold = 10, delay = 10L, delayUnit = ChronoUnit.SECONDS)
	Collection<SummitToSummitLog> fetchSummitToSummitLogsById(@QueryParam("id") String userId,
			@QueryParam("year") @DefaultValue("all") String year);
}

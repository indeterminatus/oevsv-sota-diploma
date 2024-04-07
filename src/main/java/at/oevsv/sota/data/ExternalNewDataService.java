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

import at.oevsv.sota.data.domain.SummitActivationLog;
import io.quarkus.vertx.http.Compressed;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import java.util.Collection;

@Singleton
@Path("/api")
@RegisterRestClient(configKey = "api2-db")
public interface ExternalNewDataService {

    @GET
    @Compressed
    @Path("/activations/{summit}")
    @Produces("application/json")
    Collection<SummitActivationLog> fetchActivationsForSummit(@PathParam("summit") String summit);
}

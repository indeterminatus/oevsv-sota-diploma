/*
 * Copyright (C) 2024 David Schwingenschlögl
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

import io.quarkus.runtime.configuration.MemorySize;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;

@ApplicationScoped
public class MaxRequestBodySizeFilter implements ClientRequestFilter {

    @ConfigProperty(name = "diploma.http.client.max-body-size")
    @DefaultValue("50M")
    MemorySize maxRequestBodySize;

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        requestContext.setProperty("org.eclipse.microprofile.rest.client.request.entity.processing", "BUFFERED");
        requestContext.setProperty("org.eclipse.microprofile.rest.client.max.entity.size", maxRequestBodySize.asLongValue());
    }
}

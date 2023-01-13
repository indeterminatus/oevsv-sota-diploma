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

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.cache.CacheManager;
import io.quarkus.scheduler.Scheduled;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.Duration;

@Path("/api/cache")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class CacheClearer {

    @Inject
    CacheManager cacheManager;

    @POST
    @Path("/cache/invalidate")
    @Scheduled(cron = "{cache.invalidation.cron}")
    @WithSpan(value = "Clear Caches")
    public void clearAllCaches() {
        for (final var cacheName : cacheManager.getCacheNames()) {
            cacheManager.getCache(cacheName).ifPresent(cache -> cache.invalidateAll().await().atMost(Duration.ofMinutes(1L)));
        }
    }
}

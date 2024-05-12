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

import at.oevsv.sota.data.ExternalSummitsListService;
import at.oevsv.sota.data.MaxRequestBodySizeFilter;
import at.oevsv.sota.data.domain.Summit;
import at.oevsv.sota.data.domain.SummitListEntry;
import com.opencsv.bean.CsvToBeanBuilder;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.RedirectionException;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

@Path("/api/summits")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RegisterProvider(MaxRequestBodySizeFilter.class)
public final class SummitList {

    @RestClient
    ExternalSummitsListService externalSummitsListService;

    @Inject
    ManagedExecutor executorService;

    private final AtomicBoolean initialSynchronizationCompleted = new AtomicBoolean(false);

    @POST
    @RolesAllowed("admin")
    @Blocking
    @Path("/synchronize")
    @Transactional
    public void synchronize() {
        doSynchronize();
    }

    void onStart(@Observes StartupEvent event) {
        Log.infof("Application initialized; scheduling synchronization of summit list.");
        executorService.runAsync(() -> {
            Log.infof("Application initialized; synchronizing summit list...");
            doSynchronize();
            Log.infof("Summit list synchronization completed.");
            initialSynchronizationCompleted.set(true);
        });
        Log.infof("Summit list synchronization scheduled.");
    }

    @VisibleForTesting
    boolean isInitialSynchronizationCompleted() {
        return initialSynchronizationCompleted.get();
    }

    /**
     * Actually performs the synchronization; this was extracted, because the {@link #onStart(StartupEvent)} path should
     * not depend on authorization (as it's the local application context).
     */
    @Scheduled(cron = "{summit.update.cron}")
    @WithSpan(value = "Check for summit list update")
    @Bulkhead(value = 1, waitingTaskQueue = 1)
    @VisibleForTesting
    @Transactional
    void doSynchronize() {
        String lastSummitListFetch = "";
        var lastUpdateDate = SummitListUpdateLog.lastUpdate();
        if (lastUpdateDate != null) {
            lastSummitListFetch = lastUpdateDate.atZone(ZoneId.of("UTC")).format(DateTimeFormatter.RFC_1123_DATE_TIME);
        }

        Log.infof("Checking for summit list update (after: %s)", lastSummitListFetch);
        try (final InputStream is = externalSummitsListService.fetchSummitsList(lastSummitListFetch); final Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            lastUpdateDate = LocalDateTime.now();
            final List<SummitListEntry> result = parseCsv(reader);
            lastSummitListFetch = lastUpdateDate.atZone(ZoneId.of("UTC")).format(DateTimeFormatter.RFC_1123_DATE_TIME);

            Log.infof("Received %d (filtered) entries, updating persistence", result.size());
            persistAll(result);

            final var log = new SummitListUpdateLog();
            log.setDate(lastUpdateDate);
            log.setUpdateCount(result.size());
            log.persistAndFlush();
        } catch (IOException e) {
            Log.warn("Could not update summit list.", e);
        } catch (RedirectionException e) {
            if (StringUtils.contains(e.getMessage(), "HTTP 304")) {
                Log.info("No modifications since last check.");
            } else {
                Log.warn("Could not update summit list.", e);
            }
        }

        Log.infof("Last update: %s", lastSummitListFetch);
    }

    @WithSpan(value = "Persist All Summits")
    static void persistAll(List<SummitListEntry> result) {
        int batchSize = 0;
        for (final var source : result) {
            ++batchSize;
            if (batchSize % 500 == 0) {
                Log.infof("%d / %d ...", batchSize, result.size());
            }

            final var entity = SummitListEntry.<SummitListEntry>findById(source.getSummitCode());
            if (entity == null || !entity.isPersistent()) {
                if (batchSize % 500 == 0) {
                    source.persistAndFlush();
                } else {
                    source.persist();
                }
            } else {
                entity.setSummitCode(source.getSummitCode());
                entity.setSummitName(source.getSummitName());
                entity.setValidTo(source.getValidTo());
                entity.setValidFrom(source.getValidFrom());
            }
        }
    }

    @WithSpan(value = "Parse CSV")
    @NotNull
    static List<SummitListEntry> parseCsv(Reader reader) {
        final var parser = new CsvToBeanBuilder<SummitListEntry>(reader).withSkipLines(1).withType(SummitListEntry.class).build();
        return parser.parse().stream().filter(isRelevantForOE()).toList();
    }

    @NotNull
    private static Predicate<SummitListEntry> isRelevantForOE() {
        return entry -> new Summit(entry.getSummitCode(), entry.getSummitName()).state() != null;
    }

    @GET
    @PermitAll
    @SuppressWarnings("java:S3252") // justification: SummitListEntry is better readable
    public List<SummitListEntry> list() {
        return SummitListEntry.listAll();
    }

    @GET
    @PermitAll
    @Path("/{code}")
    @SuppressWarnings("java:S3252") // justification: SummitListEntry is better readable
    public SummitListEntry getSummitListEntry(@PathParam("code") String summitCode) {
        return SummitListEntry.findById(summitCode);
    }

    @PUT
    @RolesAllowed("admin")
    @Path("/{code}")
    @Transactional
    @SuppressWarnings("java:S3252") // justification: SummitListEntry is better readable
    public SummitListEntry update(@PathParam("code") String summitCode, SummitListEntry entry) {
        final SummitListEntry entity = SummitListEntry.findById(summitCode);
        if (entity == null) {
            throw new NotFoundException();
        }

        entity.setSummitName(entry.getSummitName());
        entity.setValidFrom(entry.getValidFrom());
        entity.setValidTo(entry.getValidTo());
        return entity;
    }
}

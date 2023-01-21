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
import at.oevsv.sota.data.domain.Summit;
import at.oevsv.sota.data.domain.SummitListEntry;
import com.opencsv.bean.CsvToBeanBuilder;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jetbrains.annotations.NotNull;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.RedirectionException;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Predicate;

@Path("/api/summits")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public final class SummitList {

    @Inject
    @RestClient
    ExternalSummitsListService externalSummitsListService;

    @POST
    @Path("/synchronize")
    @Scheduled(cron = "{summit.update.cron}")
    @WithSpan(value = "Check for summit list update")
    @Bulkhead(value = 1, waitingTaskQueue = 1)
    @Transactional
    public void synchronize() {
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

    void onStart(@Observes StartupEvent event) {
        Log.infof("Application initialized; synchronizing summit list...");
        synchronize();
        Log.infof("Summit list synchronization completed.");
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
    @SuppressWarnings("java:S3252") // justification: SummitListEntry is better readable
    public List<SummitListEntry> list() {
        return SummitListEntry.listAll();
    }

    @GET
    @Path("/{code}")
    @SuppressWarnings("java:S3252") // justification: SummitListEntry is better readable
    public SummitListEntry getSummitListEntry(@PathParam("code") String summitCode) {
        return SummitListEntry.findById(summitCode);
    }

    @PUT
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

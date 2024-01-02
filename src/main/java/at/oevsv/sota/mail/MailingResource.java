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

package at.oevsv.sota.mail;

import at.oevsv.sota.data.api.Candidate;
import at.oevsv.sota.data.api.Requester;
import at.oevsv.sota.data.domain.Summit;
import at.oevsv.sota.data.persistence.DiplomaLogResource;
import at.oevsv.sota.pdf.PdfGenerationResource;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.logging.Log;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.Bulkhead;

import javax.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@ApplicationScoped
@Path("/api/mail")
public class MailingResource {

    @Inject
    MailingConfiguration configuration;

    @Inject
    PdfGenerationResource generator;

    @Inject
    DiplomaLogResource diplomaLog;

    @Inject
    ReactiveMailer mailer;

    @ConfigProperty(name = "pdf.preview.quality", defaultValue = "95")
    @Min(1L)
    @Max(100L)
    int previewQuality;

    private final ThreadFactory threadFactory =
            new ThreadFactoryBuilder().setNameFormat("mailing-worker-%d").build();
    private final ExecutorService executor = Executors.newFixedThreadPool(1, threadFactory);

    @Scheduled(cron = "{pending.requests.check.cron}")
    @Bulkhead(value = 1, waitingTaskQueue = 1)
    @Blocking
    @WithSpan(kind = SpanKind.SERVER, value = "sendPendingRequests")
    public void sendPendingRequests() {
        final var pendingRequests = diplomaLog.listPendingInternal();
        if (!pendingRequests.isEmpty()) {
            Log.infof("Sending %d pending diploma requests for review.", pendingRequests.size());
        }
        int successful = 0;
        for (final var pending : pendingRequests) {
            final var requester = DiplomaLogResource.toRequester(pending);
            final var candidate = DiplomaLogResource.toCandidate(pending);
            final var locale = DiplomaLogResource.toRequestedLocale(pending);

            try {
                sendDiplomaForReview(requester, candidate, pending.id.intValue(), locale);
                Log.debugf("Marking request with ID %d as review-mail-sent", pending.id);
                diplomaLog.markReviewMailSent(pending.id);
                Log.debugf("Marked request with ID %d as review-mail-sent", pending.id);
                ++successful;
            } catch (IOException | MissingResourceException e) {
                Log.warnf(e, "Could not send request %d for review!", pending.id);
            }
        }
        if (!pendingRequests.isEmpty()) {
            Log.infof("Sent %d (of %d) pending diploma requests for review.", successful, pendingRequests.size());
        }
    }

    @WithSpan(kind = SpanKind.SERVER, value = "sendDiplomaForReview")
    public void sendDiplomaForReview(Requester requester, Candidate candidate, int sequence, Locale locale) throws IOException {
        final var template = Templates.reviewRequest(requester, List.of(candidate));
        final var mail = new Mail();
        final var generatedPdf = generatePreviewPdf(requester, candidate, sequence, locale);
        mail.addAttachment(generator.fileNameFor(generationParameter(requester, candidate, sequence)), generatedPdf, "application/pdf");
        template.mail(mail);

        template.subject(MessageFormat.format("Diplom-Anfrage von {0} ({1})", requester.name, requester.callSign));
        template.from("\"SOTA Diplome\" <no-reply@oevsv.at>");
        template.to(configuration.recipients().toArray(new String[]{}));

        template.send()
                .runSubscriptionOn(executor)
                .onSubscription().invoke(subscription -> Log.debugf("Subscribed (%s)!", subscription))
                .onFailure().invoke(throwable -> Log.warn("Could not send mail.", throwable))
                .onItem().invoke(() -> Log.info("Successfully sent mail."))
                .onCancellation().invoke(() -> Log.warn("Sending mail cancelled."))
                .await().atMost(Duration.ofSeconds(10L));
        Log.debug("Mail sent.");
    }

    @Nonnull
    private byte[] generatePreviewPdf(Requester requester, Candidate candidate, int sequence, Locale locale) throws IOException {
        final PdfGenerationResource.Generation parameter = generationParameter(requester, candidate, sequence);
        parameter.setQuality(previewQuality / 100.0f);
        parameter.setLocale(locale);
        return generator.generatePdfBytes(parameter);
    }

    @Nonnull
    private static PdfGenerationResource.Generation generationParameter(Requester requester, Candidate candidate, int sequence) {
        final var parameter = new PdfGenerationResource.Generation(requester, candidate);
        parameter.setSequence(sequence);
        parameter.setSequenceSuffix(null);
        return parameter;
    }

    @POST
    @Path("test")
    @RolesAllowed("admin")
    @Blocking
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void testMail(@QueryParam("to") String recipient, @QueryParam("templated") @DefaultValue("true") boolean templated) throws IOException {
        final var requester = new Requester("---", recipient, "Test Empfänger");
        final var candidate = new Candidate("---", "", Candidate.Category.CHASER, Candidate.Rank.BRONZE, Map.of(Summit.State.OE5, 1L));
        if (templated) {
            sendDiplomaForReview(requester, candidate, 9999, Locale.GERMAN);
        } else {
            mailer.send(new Mail().setSubject("ÖVSV Mailing Test").addTo(recipient).setText("Auto-generated test mail")
                    .addAttachment("test.pdf", generatePreviewPdf(requester, candidate, 9999, Locale.GERMAN), "application/pdf")).await().atMost(Duration.ofMinutes(5L));
        }
    }
}

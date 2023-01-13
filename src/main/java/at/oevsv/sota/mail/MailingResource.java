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
import org.eclipse.microprofile.faulttolerance.Bulkhead;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.List;
import java.util.Map;
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

    private final ThreadFactory threadFactory =
            new ThreadFactoryBuilder().setNameFormat("mailing-worker-%d").build();
    private final ExecutorService executor = Executors.newFixedThreadPool(1, threadFactory);

    @Scheduled(cron = "{pending.requests.check.cron}")
    @Bulkhead(value = 1, waitingTaskQueue = 1)
    @Blocking
    @WithSpan(kind = SpanKind.SERVER, value = "sendPendingRequests")
    public void sendPendingRequests() {
        final var pendingRequests = diplomaLog.listPending();
        if (!pendingRequests.isEmpty()) {
            Log.infof("Sending %d pending diploma requests for review.", pendingRequests.size());
        }
        for (final var pending : pendingRequests) {
            final var requester = DiplomaLogResource.toRequester(pending);
            final var candidate = DiplomaLogResource.toCandidate(pending);

            try {
                sendDiplomaForReview(requester, candidate, pending.id.intValue());
                Log.debugf("Marking request with ID %d as review-mail-sent", pending.id);
                diplomaLog.markReviewMailSent(pending.id);
                Log.debugf("Marked request with ID %d as review-mail-sent", pending.id);
            } catch (IOException e) {
                Log.warnf(e, "Could not send request %d for review!", pending.id);
            }
        }
        if (!pendingRequests.isEmpty()) {
            Log.infof("Sent %d pending diploma requests for review.", pendingRequests.size());
        }
    }

    @WithSpan(kind = SpanKind.SERVER, value = "sendDiplomaForReview")
    public void sendDiplomaForReview(Requester requester, Candidate candidate, int sequence) throws IOException {
        final var template = Templates.reviewRequest(requester, List.of(candidate));
        final var mail = new Mail();
        final var generatedPdf = generatePdf(requester, candidate, sequence);
        mail.addAttachment(candidate.category().toString() + ".pdf", generatedPdf, "application/pdf");
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

    private byte[] generatePdf(Requester requester, Candidate candidate, int sequence) throws IOException {
        final var parameter = new PdfGenerationResource.Generation(requester, candidate);
        parameter.setQuality(0.1f);
        parameter.setSequence(sequence);
        try (final var response = generator.generatePdf(parameter)) {
            return response.readEntity(byte[].class);
        }
    }

    @POST
    @Path("test")
    @Blocking
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void testMail(@QueryParam("to") String recipient, @QueryParam("templated") @DefaultValue("true") boolean templated) throws IOException {
        final var requester = new Requester("---", recipient, "Test Empfänger");
        final var candidate = new Candidate("---", "", Candidate.Category.CHASER, Candidate.Rank.BRONZE, Map.of(Summit.State.OE5, 1L));
        if (templated) {
            sendDiplomaForReview(requester, candidate, 9999);
        } else {
            mailer.send(new Mail().setSubject("ÖVSV Mailing Test").addTo(recipient).setText("Auto-generated test mail")
                    .addAttachment("test.pdf", generatePdf(requester, candidate, 9999), "application/pdf")).await().atMost(Duration.ofMinutes(5L));
        }
    }
}

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

package at.oevsv.sota;

import at.oevsv.sota.data.WireMockExtension;
import at.oevsv.sota.data.api.DiplomaRequest;
import at.oevsv.sota.data.api.Requester;
import at.oevsv.sota.data.persistence.DiplomaLogResource;
import at.oevsv.sota.data.persistence.DiplomaLogResourceTestSeam;
import at.oevsv.sota.data.persistence.SummitList;
import at.oevsv.sota.data.persistence.SummitListTestSeam;
import at.oevsv.sota.mail.MailingResource;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@QuarkusTestResource(WireMockExtension.class)
final class FullRoundtripTest {

    @Inject
    DiplomaResource diplomaResource;

    @Inject
    SummitList summitList;

    @Inject
    DiplomaLogResource logs;

    @Inject
    MockMailbox mailbox;

    @Inject
    MailingResource mailingResource;

    private static final Object LOCK = new Object();

    @BeforeEach
    void init() {
        mailbox.clear();
    }

    @BeforeEach
    void synchronize() {
        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> SummitListTestSeam.isInitialSynchronizationCompleted(summitList));
    }

    @Test
    void requestingDiplomaSendsMail() {
        synchronized (LOCK) {
            final var candidates = diplomaResource.checkCandidatesForUser("OE5JFE", null);
            final var requester = new Requester("OE5JFE", "noreply@nothing.com", "Dip-Dip Dabbadudei");
            final var request = diplomaResource.requestDiploma("OE5JFE", new DiplomaRequest(requester, candidates, "de"));
            assertThat(request).isTrue();

            assertThat(DiplomaLogResourceTestSeam.listPendingOn(logs)).isNotEmpty();

            mailingResource.sendPendingRequests();

            final var mails = mailbox.getMailsSentTo("oe5idt@oevsv.at");
            assertThat(mails).hasSameSizeAs(candidates);
            Mail actual = mails.get(0);
            assertThat(actual.getHtml()).containsIgnoringCase(requester.callSign).contains(requester.name);
            assertThat(actual.getText()).containsIgnoringCase(requester.callSign).contains(requester.name);
            assertThat(actual.getSubject()).containsIgnoringCase(requester.callSign).contains(requester.name);
            assertThat(actual.getAttachments()).hasSize(1).singleElement().hasFieldOrPropertyWithValue("contentType", "application/pdf");

            DiplomaLogResourceTestSeam.deleteAllOn(logs);
        }
    }
}

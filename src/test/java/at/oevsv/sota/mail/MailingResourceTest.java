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
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class MailingResourceTest {

    @Inject
    MockMailbox mailbox;

    @Inject
    MailingResource sut;

    @BeforeEach
    void init() {
        mailbox.clear();
    }

    @Test
    @TestSecurity(user = "test", roles = "admin")
    void sendingMailWorks() throws IOException {
        final Requester requester = new Requester();
        requester.mail = "something@nothing.com";
        requester.callSign = "OE0QSL";
        requester.name = "Not Sure";

        sut.sendDiplomaForReview(requester, new Candidate(requester.callSign, "12345", Candidate.Category.ACTIVATOR, Candidate.Rank.BRONZE, Map.of()), 9999, Locale.GERMAN);

        final var mails = mailbox.getMailsSentTo("oe5idt@oevsv.at");
        assertThat(mails).hasSize(1);
        Mail actual = mails.get(0);
        assertThat(actual.getHtml()).contains(requester.callSign).contains(requester.name);
        assertThat(actual.getText()).contains(requester.callSign).contains(requester.name);
        assertThat(actual.getSubject()).contains(requester.callSign).contains(requester.name);
        assertThat(actual.getAttachments()).hasSize(1);
    }
}

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
import io.quarkus.mailer.MailTemplate;
import io.quarkus.qute.CheckedTemplate;

import java.util.Collection;

@CheckedTemplate
@SuppressWarnings("java:S1118") // justification: special class, must not provide constructor
public class Templates {

    public static native MailTemplate.MailTemplateInstance reviewRequest(Requester requester, Collection<Candidate> candidates);

}

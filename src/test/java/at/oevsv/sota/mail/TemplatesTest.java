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

import at.oevsv.sota.data.api.Requester;
import io.quarkus.logging.Log;
import io.quarkus.test.junit.QuarkusTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
final class TemplatesTest {

    @Test
    void requesterInBody() {
        final var template = Templates.reviewRequest(getRequester(), List.of());
        final var rendered = template.templateInstance().render();
        Log.info(rendered);
        assertThat(rendered).contains("David Schwingenschlögl");
    }

    @SuppressWarnings("ConstantConditions") // justification: passing null is the purpose of the test
    @Test
    void candidatesCanBeNull() {
        final var template = Templates.reviewRequest(getRequester(), null);
        final var rendered = template.templateInstance().render();
        Log.info(rendered);
        assertThat(rendered).isNotEmpty();
    }

    @NotNull
    private static Requester getRequester() {
        return new Requester("OE5IDT", "oe5idt@oevsv.at", "David Schwingenschlögl");
    }
}

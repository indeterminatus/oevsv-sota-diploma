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

package at.oevsv.sota;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@QuarkusTest
final class TestResourceTest {

    @Inject
    TestResource sut;

    @Test
    void loadingResourceBundleWorks() {
        assertThat(sut.checkResourceBundle("/pdf/i18n/messages", "de", "diploma.title")).isEqualTo("DIPLOM");
        assertThat(sut.checkResourceBundle("/pdf/i18n/messages", "en", "diploma.title")).isEqualTo("AWARD");
        assertThat(sut.checkResourceBundle("pdf.i18n.messages", "de", "diploma.title")).isEqualTo("DIPLOM");
        assertThat(sut.checkResourceBundle("pdf.i18n.messages", "en", "diploma.title")).isEqualTo("AWARD");
    }

    @Test
    void loadingCharsetWorks() {
        assertThat(sut.checkCharset("Cp1252")).isEqualTo("windows-1252");
        assertThat(sut.checkCharset("UTF-8")).isEqualTo("UTF-8");
        assertThat(sut.checkCharset("bla")).contains("bla").contains("is not supported");
    }

    @Test
    @TestSecurity(user = "test", roles = "admin")
    void loadingResourceWorks() {
        assertThat(sut.checkResource("/templates/reviewRequest.txt")).contains("Exists");
    }

    @Test
    @TestSecurity(user = "test", roles = "admin")
    void loadingNonExistingResource_404() {
        assertThatExceptionOfType(WebApplicationException.class).isThrownBy(() -> sut.checkResource("/passwd"));
    }
}

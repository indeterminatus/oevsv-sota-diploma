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

package at.oevsv.sota.pdf;

import at.oevsv.sota.data.WireMockExtension;
import at.oevsv.sota.data.api.Candidate;
import at.oevsv.sota.data.api.Requester;
import com.github.attiand.assertj.jaxrs.asserts.ResponseAssert;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import javax.annotation.Nonnull;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import static com.github.attiand.assertj.jaxrs.asserts.ResponseAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;


@QuarkusTest
@QuarkusTestResource(WireMockExtension.class)
final class PdfGenerationResourceTest {

    public static final String CALL_SIGN = "oe5idt";

    @Inject
    PdfGenerationResource sut;

    // region fileNameFor
    @Test
    void fileNameFor_matchesFully() {
        assertThat(PdfGenerationResource.fileNameFor(requester(), candidate(Candidate.Category.S2S, Candidate.Rank.BRONZE))).isEqualTo("oe5idt_s2s_bronze.pdf");
    }

    @Test
    void fileNameFor_contains_callSign() {
        assertThat(PdfGenerationResource.fileNameFor(requester(), candidate(Candidate.Category.S2S, Candidate.Rank.BRONZE))).contains(CALL_SIGN);
    }

    @Test
    void fileNameFor_endsWith_pdf() {
        assertThat(PdfGenerationResource.fileNameFor(requester(), candidate(Candidate.Category.S2S, Candidate.Rank.BRONZE))).endsWith(".pdf");
    }

    @ParameterizedTest
    @EnumSource
    void fileNameFor_contains_category(Candidate.Category category) {
        final String expected = category.toString().substring(0, 3).toLowerCase(Locale.ROOT);
        assertThat(PdfGenerationResource.fileNameFor(requester(), candidate(category, Candidate.Rank.BRONZE))).contains(expected);
    }

    @ParameterizedTest
    @EnumSource
    void filaNameFor_contains_rank(Candidate.Rank rank) {
        final String expected = rank.toString().toLowerCase(Locale.ROOT);
        assertThat(PdfGenerationResource.fileNameFor(requester(), candidate(Candidate.Category.S2S, rank))).contains(expected);
    }
    // endregion

    @Test
    @TestSecurity(user = "test", roles = "admin")
    void generatePdf() throws IOException {
        final var response = sut.generatePdf(new PdfGenerationResource.Generation(requester(), candidate(Candidate.Category.ACTIVATOR, Candidate.Rank.GOLD)));
        ResponseAssert.assertThat(response).hasStatusCode(200).hasMediaType(MediaType.APPLICATION_OCTET_STREAM_TYPE).hasEntity();
    }

    // region Test helpers
    @Nonnull
    private static Requester requester() {
        return new Requester(CALL_SIGN, "oe5idt@oevsv.at", "David Schwingenschlögl");
    }

    @Nonnull
    private static Candidate candidate(Candidate.Category category, Candidate.Rank rank) {
        return new Candidate(CALL_SIGN, "12345", category, rank, Map.of());
    }
    // endregion
}

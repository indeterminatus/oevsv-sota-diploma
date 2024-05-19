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
import at.oevsv.sota.data.api.Generation;
import at.oevsv.sota.data.api.Requester;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

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
        final var generation = new Generation(requester(), candidate(Candidate.Category.S2S, Candidate.Rank.BRONZE));
        assertThat(sut.fileNameFor(generation)).isEqualTo("oe5idt_s2s_bronze.pdf");
    }

    @Test
    void fileNameFor_contains_callSign() {
        final var generation = new Generation(requester(), candidate(Candidate.Category.S2S, Candidate.Rank.BRONZE));
        assertThat(sut.fileNameFor(generation)).contains(CALL_SIGN);
    }

    @Test
    void fileNameFor_endsWith_pdf() {
        final var generation = new Generation(requester(), candidate(Candidate.Category.S2S, Candidate.Rank.BRONZE));
        assertThat(sut.fileNameFor(generation)).endsWith(".pdf");
    }

    @ParameterizedTest
    @EnumSource(names = "OE20SOTA", mode = EnumSource.Mode.EXCLUDE)
    void fileNameFor_contains_category(Candidate.Category category) {
        final var generation = new Generation(requester(), candidate(category, Candidate.Rank.BRONZE));
        final String expected = category.toString().substring(0, 3).toLowerCase(Locale.ROOT);
        assertThat(sut.fileNameFor(generation)).contains(expected);
    }

    @ParameterizedTest
    @EnumSource
    void fileNameFor_contains_rank(Candidate.Rank rank) {
        final var generation = new Generation(requester(), candidate(Candidate.Category.S2S, rank));
        final String expected = rank.toString().toLowerCase(Locale.ROOT);
        assertThat(sut.fileNameFor(generation)).contains(expected);
    }
    // endregion

    // region Special handling for OE20SOTA diploma
    static Stream<Arguments> oe20_fileNames() {
        // @formatter:off
        return Stream.of(
            Arguments.of(1, "x", "oe20_0001x_oe5idt.pdf"),
            Arguments.of(1, null, "oe20_0001_oe5idt.pdf"),
            Arguments.of(9999, null, "oe20_9999_oe5idt.pdf"),
            Arguments.of(10_000, null, "oe20_10000_oe5idt.pdf"),
            Arguments.of(-1, "x", "oe20_oe5idt.pdf"),
            Arguments.of(0, null, "oe20_oe5idt.pdf")
        );
        // @formatter:on
    }

    @ParameterizedTest
    @MethodSource("oe20_fileNames")
    void oe20_fileNameFor_matchesFully(int sequence, String suffix, String expected) {
        final var generation = new Generation(requester(), candidate(Candidate.Category.OE20SOTA, Candidate.Rank.NONE));
        generation.setSequence(sequence);
        generation.setSequenceSuffix(suffix);

        final var fileName = sut.fileNameFor(generation);
        assertThat(fileName).isEqualTo(expected);
    }
    // endregion

    @Test
    @TestSecurity(user = "test", roles = "admin")
    void generatePdf() throws IOException {
        final var response = sut.generatePdf(new Generation(requester(), candidate(Candidate.Category.ACTIVATOR, Candidate.Rank.GOLD)));
        assertThat(response).hasStatusCode(200).hasMediaType(MediaType.valueOf("application/pdf")).hasEntity();
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

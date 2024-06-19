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

package at.oevsv.sota.pdf.diploma;

import at.oevsv.sota.data.api.Candidate;
import at.oevsv.sota.data.api.Generation;
import at.oevsv.sota.data.api.Requester;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
final class DiplomaFormatsTest {

    @Inject
    DiplomaFormats sut;

    static Stream<Arguments> fileNames() {
        // @formatter:off
        return Stream.of(
            Arguments.of(Candidate.Category.OE20SOTA, Candidate.Rank.NONE, 1, "oe20_0001_oe5idt.pdf"),
            Arguments.of(Candidate.Category.S2S, Candidate.Rank.BRONZE, 1, "oe5idt_s2s_bronze.pdf"),
            Arguments.of(Candidate.Category.S2S, Candidate.Rank.SILVER, 1, "oe5idt_s2s_silver.pdf"),
            Arguments.of(Candidate.Category.S2S, Candidate.Rank.GOLD, 1, "oe5idt_s2s_gold.pdf"),
            Arguments.of(Candidate.Category.ACTIVATOR, Candidate.Rank.BRONZE, 1, "oe5idt_act_bronze.pdf"),
            Arguments.of(Candidate.Category.ACTIVATOR, Candidate.Rank.SILVER, 1, "oe5idt_act_silver.pdf"),
            Arguments.of(Candidate.Category.ACTIVATOR, Candidate.Rank.GOLD, 1, "oe5idt_act_gold.pdf"),
            Arguments.of(Candidate.Category.CHASER, Candidate.Rank.BRONZE, 1, "oe5idt_cha_bronze.pdf"),
            Arguments.of(Candidate.Category.CHASER, Candidate.Rank.SILVER, 1, "oe5idt_cha_silver.pdf"),
            Arguments.of(Candidate.Category.CHASER, Candidate.Rank.GOLD, 1, "oe5idt_cha_gold.pdf")
        );
        // @formatter:on
    }

    static Stream<Arguments> ids() {
        // @formatter:off
        return Stream.of(
                Arguments.of(Candidate.Category.OE20SOTA, Candidate.Rank.NONE, 1, "OE20-0001"),
                Arguments.of(Candidate.Category.S2S, Candidate.Rank.BRONZE, 1, "S2S-BR-0001"),
                Arguments.of(Candidate.Category.S2S, Candidate.Rank.SILVER, 1, "S2S-SI-0001"),
                Arguments.of(Candidate.Category.S2S, Candidate.Rank.GOLD, 1, "S2S-GO-0001"),
                Arguments.of(Candidate.Category.ACTIVATOR, Candidate.Rank.BRONZE, 1, "ACT-BR-0001"),
                Arguments.of(Candidate.Category.ACTIVATOR, Candidate.Rank.SILVER, 1, "ACT-SI-0001"),
                Arguments.of(Candidate.Category.ACTIVATOR, Candidate.Rank.GOLD, 1, "ACT-GO-0001"),
                Arguments.of(Candidate.Category.CHASER, Candidate.Rank.BRONZE, 1, "CHA-BR-0001"),
                Arguments.of(Candidate.Category.CHASER, Candidate.Rank.SILVER, 1, "CHA-SI-0001"),
                Arguments.of(Candidate.Category.CHASER, Candidate.Rank.GOLD, 1, "CHA-GO-0001")
        );
        // @formatter:on
    }

    @ParameterizedTest
    @MethodSource("fileNames")
    void fileNameHasExpectedValue(Candidate.Category category, Candidate.Rank rank, int sequence, String fileName) {
        final var generation = new Generation(new Requester("oe5idt", "test@test.com", "David"), new Candidate("oe5idt", "0", category, rank, Map.of()));
        generation.setSequence(sequence);

        final var format = sut.generationStrategyFor(generation);
        assertThat(format).isNotNull();
        assertThat(format.fileNameFor(generation)).isEqualTo(fileName);
    }

    @ParameterizedTest
    @MethodSource("ids")
    void idHasExpectedValue(Candidate.Category category, Candidate.Rank rank, int sequence, String id) {
        final var generation = new Generation(new Requester("oe5idt", "test@test.com", "David"), new Candidate("oe5idt", "0", category, rank, Map.of()));
        generation.setSequence(sequence);

        final var format = sut.generationStrategyFor(generation);
        assertThat(format).isNotNull();
        assertThat(format.idFor(generation)).isEqualTo(id);
    }
}

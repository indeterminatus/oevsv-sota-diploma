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

package at.oevsv.sota.data.domain.jackson;

import at.oevsv.sota.data.domain.Summit;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.annotations.VisibleForTesting;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Allows deserializing a summit from a single JSON field. The value of that field contains both the summit's code and
 * its name, and needs to be split into the respective parts.
 * <p>
 * Example values:
 * <ul>
 *     <li>"OE/OO-073 (Schoberstein)"</li>
 * </ul>
 *
 * @author schwingenschloegl
 */
public final class RegexSummitDeserializer extends StdDeserializer<Summit> {

    private static final Pattern EXTRACTOR = Pattern.compile("^(?<code>\\S+)\\s*\\((?<name>.*)\\)$");

    public RegexSummitDeserializer() {
        super(Summit.class);
    }

    @Override
    public Summit deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        final JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        if (node.isTextual()) {
            final String combinedValue = node.textValue();
            return extractSummitFromString(combinedValue);
        }

        return null;
    }

    @Nullable
    @VisibleForTesting
    static Summit extractSummitFromString(@Nullable String combinedValue) {
        if (combinedValue == null) {
            return null;
        }

        final var matcher = EXTRACTOR.matcher(combinedValue);
        if (matcher.matches()) {
            final var extractedCode = matcher.group("code");
            final var extractedName = matcher.group("name");

            return new Summit(extractedCode, extractedName);
        }

        // invariant: if control reaches this point, there is no valid summit to extract
        return null;
    }
}

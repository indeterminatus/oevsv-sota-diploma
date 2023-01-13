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

package at.oevsv.sota.data.api;

import at.oevsv.sota.data.domain.jackson.CanonicalJson;
import io.quarkus.logging.Log;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Wraps a {@link Candidate} and a corresponding signature for integrity verification.
 *
 * @param candidate
 * @param signature
 * @author schwingenschloegl
 */
public record SignedCandidate(Candidate candidate, String signature) {

    private static final String ALGORITHM = "HmacSHA256";

    public static SignedCandidate sign(Candidate candidate) {
        final var signature = generateSignature(candidate);
        return new SignedCandidate(candidate, signature);
    }

    public void verifyIntegrity() throws IllegalStateException {
        final var recreated = generateSignature(candidate);
        if (!StringUtils.equals(recreated, signature)) {
            throw new IllegalStateException("Integrity of signed candidate cannot be verified!");
        }
    }

    private static String generateSignature(Candidate candidate) {
        final var canonical = CanonicalJson.calculate(candidate);
        if (canonical == null) {
            return null;
        }

        // invariant: canonical != null
        try {
            // TODO: generate key once
            return hmac(ALGORITHM, canonical, "test123");
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            Log.warnf(e, "Could not sign canonical JSON.");
            return null;
        }
    }

    // TODO: move to class of its own
    private static String hmac(String algorithm, String data, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm);
        Mac mac = Mac.getInstance(algorithm);
        mac.init(keySpec);
        return DatatypeConverter.printHexBinary(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }
}

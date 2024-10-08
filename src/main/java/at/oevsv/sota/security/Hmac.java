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

package at.oevsv.sota.security;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import jakarta.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Handling of HMAC. A key is generated once per VM start. Signed payloads are thus accepted only within the same VM
 * life-cycle. (Re-)starting the application invalidates all previous signatures.
 */
public final class Hmac {

    private static final String ALGORITHM = "HmacSHA256";

    private static final class Holder {

        public static final SecretKey KEY;

        static {
            try {
                KEY = KeyGenerator.getInstance(ALGORITHM).generateKey();
            } catch (NoSuchAlgorithmException e) {
                // There's something seriously wrong with the platform's security provider. We cannot run here!
                throw new AssertionError(e);
            }
        }
    }

    private Hmac() {
        throw new AssertionError();
    }

    public static String calculate(String data) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(Holder.KEY);
        return DatatypeConverter.printHexBinary(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }
}

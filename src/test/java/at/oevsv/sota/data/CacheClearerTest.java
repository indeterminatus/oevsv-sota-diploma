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

package at.oevsv.sota.data;

import io.quarkus.security.UnauthorizedException;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

@QuarkusTest
@QuarkusTestResource(WireMockExtension.class)
final class CacheClearerTest {

    @Inject
    CacheClearer sut;

    @Test
    void clearingCachesInternallyIsAllowed() {
        assertThatNoException().isThrownBy(() -> sut.doClearAllCaches());
    }

    @Test
    @TestSecurity(user = "test", roles = "admin")
    void clearingCachesWithAuthenticationIsAllowed() {
        assertThatNoException().isThrownBy(() -> sut.clearAllCaches());
    }

    @Test
    void clearingCachesWithoutAuthenticationIsForbidden() {
        assertThatExceptionOfType(UnauthorizedException.class).isThrownBy(() -> sut.clearAllCaches());
    }
}

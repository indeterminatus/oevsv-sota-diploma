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

package at.oevsv.sota.data.persistence;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
final class SummitListExternalTest {

    @Inject
    SummitList sut;

    @BeforeEach
    void synchronize() {
        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> SummitListTestSeam.isInitialSynchronizationCompleted(sut));
    }

    @Test
    @TestSecurity(user = "test", roles = "admin")
    void synchronizeCanBeCalledInSuccession() {
        sut.synchronize();
        final var firstFetch = sut.list();
        assertThat(firstFetch).isNotEmpty();
        sut.synchronize();
        assertThat(sut.list()).hasSameSizeAs(firstFetch);
    }
}

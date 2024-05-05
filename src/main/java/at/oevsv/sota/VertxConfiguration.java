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

package at.oevsv.sota;

import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Produces;

@ApplicationScoped
public class VertxConfiguration {

    @Produces
    @Singleton
    public VertxOptions customizeVertx() {
        VertxOptions options = new VertxOptions();
        options.setMaxEventLoopExecuteTime(Long.MAX_VALUE);
        options.setMaxWorkerExecuteTime(Long.MAX_VALUE);

        return options;
    }

    @Produces
    @Singleton
    public HttpServerOptions customizeHttpServerOptions() {
        // Assuming 50 MB limit, you need to set this in bytes
        long maxBodySize = 50L * 1024L * 1024L; // 50 MB in bytes

        // Set the max chunk size
        return new HttpServerOptions()
                .setMaxInitialLineLength(4096) // Adjust if needed
                .setMaxHeaderSize(8192) // Adjust if needed
                .setMaxChunkSize((int) maxBodySize);
    }
}

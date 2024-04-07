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

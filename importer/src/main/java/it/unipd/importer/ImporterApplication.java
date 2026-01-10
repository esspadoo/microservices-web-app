package it.unipd.importer;

import it.unipd.importer.config.ElasticConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main entry point of the Importer Spring Boot application.
 *
 * <p>This class bootstraps the Spring context and starts the embedded web server.
 * The application exposes REST APIs used to import NDJSON files into Elasticsearch.
 *
 * <p>The {@link EnableAsync} annotation enables asynchronous method execution,
 * allowing long-running tasks (such as bulk indexing) to be executed in background
 * threads without blocking HTTP requests.
 */
@SpringBootApplication
@EnableConfigurationProperties(ElasticConfig.class)
@EnableAsync
public class ImporterApplication {

    /**
     * Starts the Spring Boot application.
     *
     * @param args command-line arguments that can be passed to the application
     */
    public static void main(String[] args) {

        SpringApplication.run(ImporterApplication.class, args);
    }

}

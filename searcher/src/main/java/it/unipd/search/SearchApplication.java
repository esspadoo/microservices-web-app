package it.unipd.search;

import it.unipd.search.config.ElasticsearchProperties;
import it.unipd.search.config.InfererProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Main entry point of the Spring Boot search application.
 *
 * <p>This class bootstraps the application and serves as the primary
 * configuration source. The {@link SpringBootApplication} annotation
 * enables component scanning, auto-configuration and configuration
 * class registration.</p>
 *
 * <p>External configuration properties are enabled via
 * {@link EnableConfigurationProperties}, allowing structured access
 * to Elasticsearch and inference service settings.</p>
 *
 * <p>MongoDB repositories are detected and registered by enabling
 * {@link EnableMongoRepositories} on the designated base package.</p>
 *
 */
@SpringBootApplication
@EnableConfigurationProperties({InfererProperties.class, ElasticsearchProperties.class})
@EnableMongoRepositories(basePackages = "it.unipd.search.repository")
public class SearchApplication {

    /**
     * Application entry point.
     *
     * @param args command-line arguments passed at startup
     */
    public static void main(String[] args) {
        SpringApplication.run(SearchApplication.class, args);
    }
}

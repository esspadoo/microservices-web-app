package it.unipd.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Component responsible for initializing Elasticsearch indices at application startup.
 *
 * <p>This class checks for the existence of required indices and creates them
 * if they are not already present. The initialization logic is executed once
 * after the Spring context has been fully initialized.</p>
 */
@Component
public class ElasticsearchIndexInitializer {

    /** Elasticsearch client used to manage index operations. */
    private final ElasticsearchClient client;

    /**
     * Constructs an {@code ElasticsearchIndexInitializer}.
     *
     * @param client Elasticsearch client used to interact with the cluster
     */
    public ElasticsearchIndexInitializer(ElasticsearchClient client) {
        this.client = client;
    }

    /**
     * Initializes Elasticsearch indices after bean construction.
     *
     * <p>This method is automatically invoked by the Spring container due to
     * the {@link PostConstruct} annotation. It ensures that all required
     * indices are present before the application starts serving requests.</p>
     *
     * @throws IOException if communication with Elasticsearch fails
     */
    @PostConstruct
    public void init() throws IOException {
        createGuardianIndex();
        createOwiIndex();
    }

    /**
     * Creates the {@code guardian} index if it does not already exist.
     *
     * <p>The index is created with mappings suitable for articles originating
     * from The Guardian, including an identifier, URL, title and main content.</p>
     *
     * <p>If the index already exists, the method exits without performing
     * any operation, making the initialization idempotent.</p>
     *
     * @throws IOException if an error occurs while communicating with Elasticsearch
     */
    private void createGuardianIndex() throws IOException {
        if (client.indices().exists(e -> e.index("guardian")).value()) {
            return;
        }

        client.indices().create(c -> c
                .index("guardian")
                .mappings(m -> m
                        .properties("id", p -> p.keyword(k -> k))
                        .properties("url", p -> p.keyword(k -> k))
                        .properties("title", p -> p.text(t -> t))
                        .properties("main_content", p -> p.text(t -> t))
                )
        );
    }

    /**
     * Creates the {@code owi} index if it does not already exist.
     *
     * <p>This index is intended for OWI documents and contains mappings
     * for URL, title and main textual content.</p>
     *
     * <p>If the index already exists, the method exits without performing
     * any operation, ensuring safe repeated application startups.</p>
     *
     * @throws IOException if an error occurs while communicating with Elasticsearch
     */
    private void createOwiIndex() throws IOException {
        if (client.indices().exists(e -> e.index("owi")).value()) {
            return;
        }

        client.indices().create(c -> c
                .index("owi")
                .mappings(m -> m
                        .properties("url", p -> p.keyword(k -> k))
                        .properties("title", p -> p.text(t -> t))
                        .properties("main_content", p -> p.text(t -> t))
                )
        );
    }
}

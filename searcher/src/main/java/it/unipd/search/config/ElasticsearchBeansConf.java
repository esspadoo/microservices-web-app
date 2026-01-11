package it.unipd.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration class responsible for creating and exposing
 * Elasticsearch-related beans.
 *
 * <p>This configuration initializes the official Elasticsearch Java client
 * using the low-level REST client and makes it available as a Spring bean
 * for dependency injection.</p>
 */
@Configuration
public class ElasticsearchBeansConf {

    /** Configuration properties containing Elasticsearch connection settings. */
    private final ElasticsearchProperties properties;

    /**
     * Constructs an {@code ElasticsearchBeansConf}.
     *
     * @param properties configuration properties providing the Elasticsearch URL
     */
    public ElasticsearchBeansConf(ElasticsearchProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates and exposes an {@link ElasticsearchClient} bean.
     *
     * <p>The client is built on top of the low-level {@link RestClient}
     * and uses a {@link JacksonJsonpMapper} for JSON serialization
     * and deserialization.</p>
     *
     * <p>The Elasticsearch endpoint is read from {@link ElasticsearchProperties}.</p>
     *
     * @return a configured {@link ElasticsearchClient} instance
     */
    @Bean
    public ElasticsearchClient elasticsearchClient() {
        RestClient restClient = RestClient.builder(
                HttpHost.create(properties.getUrl())
        ).build();

        ElasticsearchTransport transport = new RestClientTransport(
                restClient,
                new JacksonJsonpMapper()
        );

        return new ElasticsearchClient(transport);
    }
}

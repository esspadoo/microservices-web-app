package it.unipd.importer.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Elasticsearch clients.
 *
 * <p>This class defines the Spring beans required to communicate with
 * an Elasticsearch cluster using the official Elasticsearch Java API Client.
 */
@Configuration
@ConfigurationProperties(prefix = "elasticsearch.server")
public class ElasticConfig {

    /**
     * URL of the Elasticsearch node.
     */
    private String url;

    /**
     * Returns the configured Elasticsearch URL.
     *
     * @return the Elasticsearch node URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the Elasticsearch node URL.
     *
     * @param url the URL to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Creates a low-level {@link RestClient} for communicating with Elasticsearch.
     *
     * <p>The client connects to the Elasticsearch instance at the configured {@code url}.
     *
     * @return a configured {@link RestClient} instance
     */
    @Bean
    public RestClient restClient() {
        return RestClient.builder(HttpHost.create(url)).build();
    }

    /**
     * Creates a high-level {@link ElasticsearchClient} using the provided {@link RestClient}.
     *
     * <p>The client uses Jackson for JSON serialization and deserialization.
     *
     * @param restClient the low-level REST client
     * @return a configured {@link ElasticsearchClient} instance
     */
    @Bean
    public ElasticsearchClient elasticsearchClient(RestClient restClient) {
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }
}

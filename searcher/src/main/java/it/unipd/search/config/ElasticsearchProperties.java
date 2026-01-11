package it.unipd.search.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for connecting to an Elasticsearch server.
 *
 * <p>This class binds external configuration values (e.g. from
 * {@code application.yml} or {@code application.properties})
 * using the prefix {@code elasticsearch.server}.</p>
 */
@ConfigurationProperties(prefix = "elasticsearch.server")
public class ElasticsearchProperties {

    /**
     * URL of the Elasticsearch node.
     *
     * <p>Example: {@code http://localhost:9200}</p>
     */
    private String url;

    /**
     * Returns the Elasticsearch server URL.
     *
     * @return the Elasticsearch node URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the Elasticsearch server URL.
     *
     * @param url the Elasticsearch node URL
     */
    public void setUrl(String url) {
        this.url = url;
    }
}

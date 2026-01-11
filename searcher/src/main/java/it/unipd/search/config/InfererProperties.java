package it.unipd.search.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the external inference service.
 *
 * <p>This class binds external configuration values (e.g. from
 * {@code application.yml} or {@code application.properties})
 * using the prefix {@code inferer}.</p>
 */
@ConfigurationProperties(prefix = "inferer")
public class InfererProperties {

    /**
     * Base URL of the inference service.
     *
     * <p>Example: {@code http://inferer:5050}</p>
     */
    private String baseUrl;

    /**
     * Returns the base URL of the inference service.
     *
     * @return the inference service base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Sets the base URL of the inference service.
     *
     * @param baseUrl the inference service base URL
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}

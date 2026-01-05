package it.unipd.search.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the external inference service.
 *
 * <p>Properties are bound from configuration files using the
 * {@code inferer.*} prefix.</p>
 */
@ConfigurationProperties(prefix = "inferer")
public class InfererProperties {

    /**
     * Base URL of the inference service.
     */
    private String baseUrl;

    /**
     * Base URL of the inference service.
     *
     * @return base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Set the base URL of the inference service.
     *
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}

package it.unipd.search.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Spring configuration class responsible for defining REST client beans.
 *
 * <p>This configuration exposes a {@link RestTemplate} instance that can be
 * injected into other components to perform HTTP communication with
 * external services.</p>
 */
@Configuration
public class RestClientConfig {

    /**
     * Creates and exposes a {@link RestTemplate} bean.
     *
     * <p>The {@code RestTemplate} is used as a synchronous HTTP client for
     * invoking external REST services.</p>
     *
     * @return a {@link RestTemplate} instance
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

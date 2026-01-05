package it.unipd.search.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Spring configuration class for REST client components.
 */
@Configuration
public class RestClientConfig {

    /**
     * Provides a {@link RestTemplate} bean for HTTP communication.
     *
     * @return configured {@link RestTemplate}
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

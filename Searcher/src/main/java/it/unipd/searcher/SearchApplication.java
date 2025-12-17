package it.unipd.searcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Spring Boot search application.
 * <p>
 * This class is annotated with {@link SpringBootApplication}, which indicates that it is the
 * primary configuration class for the Spring Boot application. It enables component scanning,
 * auto-configuration, and configuration properties scanning.
 * </p>
 * <p>
 * The {@link #main(String[])} method launches the application by invoking
 * {@link SpringApplication#run(Class, String...)}.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * java -jar search-application.jar
 * }</pre>
 * </p>
 * <p>
 * Once started, the Spring Boot application initializes the application context and all configured
 * beans, and starts any embedded web server (if applicable).
 * </p>
 */
@SpringBootApplication
public class SearchApplication {

    /**
     * Application entry point.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {

        SpringApplication.run(SearchApplication.class, args);

    }

}

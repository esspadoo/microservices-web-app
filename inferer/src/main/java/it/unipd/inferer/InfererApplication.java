package it.unipd.inferer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point of the INFERER Spring Boot application.
 * <p>
 * This class is responsible for bootstrapping and launching the application.
 * It enables component scanning, auto-configuration and dependency injection
 * through the {@link SpringBootApplication} annotation.
 * </p>
 *
 * <p>
 * The application is started by invoking the {@link #main(String[])} method,
 * which delegates control to the Spring Boot framework.
 * </p>
 */
@SpringBootApplication
public class InfererApplication {

    /**
     * Application startup method.
     * <p>
     * This method initializes the Spring application context, starts the
     * embedded web server and makes all configured REST endpoints available.
     * </p>
     *
     * @param args command-line arguments passed to the application at startup
     */
    public static void main(String[] args) {
        SpringApplication.run(InfererApplication.class, args);
    }

}


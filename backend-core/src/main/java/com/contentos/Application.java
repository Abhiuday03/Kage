package com.contentos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * WHAT IT DOES:
 * Main entry point for the ContentOS Spring Boot application.
 *
 * WHY IT EXISTS:
 * Spring Boot requires an @SpringBootApplication annotated class to bootstrap the app.
 *
 * HOW IT CONNECTS:
 * Run this class to start the entire backend API server.
 * All controllers, services, and configurations are auto-scanned from this package.
 */
@SpringBootApplication
@EnableJpaAuditing
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

package com.congen

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Main Spring Boot application class for the Congen Exercise API.
 *
 * This is the entry point for the Congen application, which provides a REST API
 * for workout generation and exercise management. The application uses Spring Boot
 * for dependency injection, web services, and database connectivity.
 *
 * The application supports multiple environments (development, staging, production)
 * and includes health checks, CORS configuration, and security headers.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@SpringBootApplication
class CongenApplication

/**
 * Main function that starts the Congen application.
 *
 * This function initializes the Spring Boot application context and logs
 * the startup process. It also sets DNS cache TTL to 30 seconds to ensure
 * fresh DNS lookups for database connections.
 *
 * @param args Command line arguments passed to the application
 */
fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger(CongenApplication::class.java)

    // Set the DNS cache TTL to 30 seconds.  This value should be less than the minimum possible
    // TTL for database connections so that a new connection does not use the cached DNS entry
    // of the connection it replaces.
    logger.info("Starting Congen application")
    val context = runApplication<CongenApplication>(*args)
    val environment = context.environment.getProperty("spring.profiles.active")
    logger.info("Running in environment: {}", environment)
}

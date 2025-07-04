package com.congen

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CongenApplication

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

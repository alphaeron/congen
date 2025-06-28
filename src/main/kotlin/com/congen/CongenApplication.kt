package com.congen

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CongenApplication

fun main(args: Array<String>) {
	// Set the DNS cache TTL to 30 seconds.  This value should be less than the minimum possible
	// TTL for database connections so that a new connection does not use the cached DNS entry
	// of the connection it replaces.
	val context = runApplication<CongenApplication>(*args)
	val environment = context.environment.getProperty("spring.profiles.active")
	// TODO slf4j for logging?
	// logger.info("Running in environment %s", environment)
}

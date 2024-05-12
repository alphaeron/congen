package com.congen

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CongenApplication

fun main(args: Array<String>) {
	val context = runApplication<CongenApplication>(*args)
	val environment = context.environment.getProperty("spring.profiles.active")
	// logger.info("Running in environment %s", environment)
}

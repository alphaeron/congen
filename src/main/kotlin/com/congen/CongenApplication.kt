package com.congen

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CongenApplication

fun main(args: Array<String>) {
	runApplication<CongenApplication>(*args)
}

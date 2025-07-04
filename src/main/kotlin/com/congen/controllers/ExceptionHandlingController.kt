package com.congen.controllers

import com.congen.exceptions.InvalidResultException
import com.congen.exceptions.NoResultsFoundException
import com.congen.exceptions.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
public class ExceptionHandlingController {
    companion object {
        private val logger = LoggerFactory.getLogger(ExceptionHandlingController::class.java)
    }

    // Exception handling methods

    // Convert a predefined exception to an HTTP Status code
    @ResponseStatus(
        value = HttpStatus.CONFLICT,
        reason = "Data integrity violation",
    ) // 409
    @ExceptionHandler(InvalidResultException::class)
    fun conflict() {
        logger.warn("Data integrity violation occurred - InvalidResultException")
    }

    // Handle NoResultsFoundException globally to return 404
    @ExceptionHandler(NoResultsFoundException::class)
    fun handleNoResultsFound(exception: NoResultsFoundException): ResponseEntity<String> {
        logger.warn("No results found: {}", exception.message ?: "Unknown error")
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Resource not found")
    }

    // Handle ValidationException globally to return 422
    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(exception: ValidationException): ResponseEntity<Map<String, String>> {
        logger.error("Validation error occurred: {}", exception.message ?: "Unknown validation error")
        return ResponseEntity.status(
            HttpStatus.UNPROCESSABLE_ENTITY,
        ).body(mapOf("error" to (exception.message ?: "Unknown validation error")))
    }
}

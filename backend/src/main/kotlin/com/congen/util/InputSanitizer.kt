package com.congen.util

import com.congen.exceptions.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.regex.Pattern

/**
 * Utility class for input sanitization and validation.
 *
 * This class provides comprehensive input validation and sanitization to prevent
 * XSS, injection attacks, and other security vulnerabilities. It implements
 * defense-in-depth by validating inputs at multiple levels.
 *
 * ## Security Features
 *
 * - **HTML Sanitization**: Removes potentially dangerous HTML/script content
 * - **SQL Injection Prevention**: Validates inputs for SQL injection patterns
 * - **XSS Prevention**: Sanitizes user inputs to prevent cross-site scripting
 * - **Path Traversal Prevention**: Validates file paths and URLs
 * - **Input Length Validation**: Enforces reasonable input length limits
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class InputSanitizer {
    companion object {
        private val logger = LoggerFactory.getLogger(InputSanitizer::class.java)
        
        // Patterns for detecting malicious content
        private val HTML_PATTERN = Pattern.compile("<[^>]*>", Pattern.CASE_INSENSITIVE)
        private val SCRIPT_TAG_PATTERN = Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        private val SCRIPT_PATTERN = Pattern.compile("javascript:|vbscript:|onload=|onerror=|onclick=", Pattern.CASE_INSENSITIVE)
        private val SQL_INJECTION_PATTERN = Pattern.compile("(union|select|insert|update|delete|drop|create|alter|exec|execute|script|javascript|vbscript)", Pattern.CASE_INSENSITIVE)
        private val PATH_TRAVERSAL_PATTERN = Pattern.compile("(\\.\\./|\\.\\\\|%2e%2e%2f|%2e%2e%5c)", Pattern.CASE_INSENSITIVE)
        
        // Maximum input lengths
        private const val MAX_STRING_LENGTH = 1000
        private const val MAX_NAME_LENGTH = 255
        private const val MAX_URL_LENGTH = 2048
    }

    /**
     * Sanitizes a string input by removing potentially dangerous content.
     *
     * @param input The input string to sanitize
     * @param fieldName The name of the field for error reporting
     * @return Sanitized string
     * @throws ValidationException if input contains malicious content
     */
    fun sanitizeString(input: String?, fieldName: String): String? {
        if (input == null) {
            return null
        }

        // Remove script tags and their content first
        var sanitized = SCRIPT_TAG_PATTERN.matcher(input).replaceAll("")
        
        // Remove remaining HTML tags
        sanitized = HTML_PATTERN.matcher(sanitized).replaceAll("")
        
        // Trim whitespace
        val trimmed = sanitized.trim()
        
        // Check for malicious patterns in the trimmed content
        if (trimmed.isNotEmpty() && containsMaliciousContent(trimmed)) {
            val message = "Input contains potentially malicious content: $fieldName"
            logger.warn(message)
            throw ValidationException(message)
        }

        // Validate length
        if (trimmed.length > MAX_STRING_LENGTH) {
            val message = "Input too long for field: $fieldName (max: $MAX_STRING_LENGTH)"
            logger.warn(message)
            throw ValidationException(message)
        }

        return trimmed
    }

    /**
     * Sanitizes a name field with specific validation rules.
     *
     * @param name The name to sanitize
     * @param fieldName The name of the field for error reporting
     * @return Sanitized name
     * @throws ValidationException if name is invalid
     */
    fun sanitizeName(name: String?, fieldName: String): String? {
        if (name.isNullOrBlank()) {
            return name
        }

        val sanitized = sanitizeString(name, fieldName)
        
        // Additional name-specific validation
        if (sanitized != null && sanitized.length > MAX_NAME_LENGTH) {
            val message = "Name too long for field: $fieldName (max: $MAX_NAME_LENGTH)"
            logger.warn(message)
            throw ValidationException(message)
        }

        return sanitized
    }

    /**
     * Validates and sanitizes a URL.
     *
     * @param url The URL to validate
     * @param fieldName The name of the field for error reporting
     * @return Sanitized URL
     * @throws ValidationException if URL is invalid
     */
    fun sanitizeUrl(url: String?, fieldName: String): String? {
        if (url.isNullOrBlank()) {
            return url
        }

        val sanitized = sanitizeString(url, fieldName)
        
        if (sanitized != null) {
            // Check for path traversal
            if (PATH_TRAVERSAL_PATTERN.matcher(sanitized).find()) {
                val message = "URL contains path traversal attempt: $fieldName"
                logger.warn(message)
                throw ValidationException(message)
            }

            // Validate URL length
            if (sanitized.length > MAX_URL_LENGTH) {
                val message = "URL too long for field: $fieldName (max: $MAX_URL_LENGTH)"
                logger.warn(message)
                throw ValidationException(message)
            }

            // Basic URL format validation
            if (!isValidUrlFormat(sanitized)) {
                val message = "Invalid URL format for field: $fieldName"
                logger.warn(message)
                throw ValidationException(message)
            }
        }

        return sanitized
    }

    /**
     * Validates a file upload by checking file extension and size.
     *
     * @param fileName The name of the uploaded file
     * @param fileSize The size of the file in bytes
     * @param allowedExtensions Set of allowed file extensions
     * @param maxSize Maximum file size in bytes
     * @return true if file is valid
     * @throws ValidationException if file is invalid
     */
    fun validateFileUpload(
        fileName: String?,
        fileSize: Long,
        allowedExtensions: Set<String>,
        maxSize: Long
    ): Boolean {
        if (fileName.isNullOrBlank()) {
            throw ValidationException("File name cannot be empty")
        }

        // Check for path traversal in filename
        if (PATH_TRAVERSAL_PATTERN.matcher(fileName).find()) {
            val message = "File name contains path traversal attempt"
            logger.warn(message)
            throw ValidationException(message)
        }

        // Check file extension
        val extension = fileName.substringAfterLast('.', "").lowercase()
        if (extension.isBlank() || !allowedExtensions.contains(extension)) {
            val message = "File type not allowed. Allowed types: ${allowedExtensions.joinToString(", ")}"
            logger.warn(message)
            throw ValidationException(message)
        }

        // Check file size
        if (fileSize > maxSize) {
            val message = "File too large. Maximum size: ${maxSize / 1024 / 1024}MB"
            logger.warn(message)
            throw ValidationException(message)
        }

        return true
    }

    /**
     * Checks if input contains malicious content.
     *
     * @param input The input to check
     * @return true if malicious content is detected
     */
    private fun containsMaliciousContent(input: String): Boolean {
        return SCRIPT_PATTERN.matcher(input).find() ||
               SQL_INJECTION_PATTERN.matcher(input).find() ||
               PATH_TRAVERSAL_PATTERN.matcher(input).find()
    }

    /**
     * Validates basic URL format.
     *
     * @param url The URL to validate
     * @return true if URL format is valid
     */
    private fun isValidUrlFormat(url: String): Boolean {
        return url.matches(Regex("^https?://[\\w\\-._~:/?#\\[\\]@!\\$&'\\(\\)\\*\\+,;=.]+$"))
    }
}

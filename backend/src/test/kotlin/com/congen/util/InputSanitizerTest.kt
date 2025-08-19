package com.congen.util

import com.congen.exceptions.ValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for InputSanitizer security validation.
 *
 * Tests comprehensive input sanitization and validation to ensure
 * security measures work correctly and prevent malicious inputs.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class InputSanitizerTest {
    private val sanitizer = InputSanitizer()

    @Test
    fun `should sanitize normal string input`() {
        val result = sanitizer.sanitizeString("Hello World", "test_field")
        assertEquals("Hello World", result)
    }

    @Test
    fun `should remove HTML tags`() {
        val result = sanitizer.sanitizeString("<script>alert('xss')</script>Hello", "test_field")
        assertEquals("Hello", result)
    }

    @Test
    fun `should reject script injection attempts`() {
        assertThrows<ValidationException> {
            sanitizer.sanitizeString("javascript:alert('xss')", "test_field")
        }
    }

    @Test
    fun `should reject SQL injection attempts`() {
        assertThrows<ValidationException> {
            sanitizer.sanitizeString("'; DROP TABLE users; --", "test_field")
        }
    }

    @Test
    fun `should reject path traversal attempts`() {
        assertThrows<ValidationException> {
            sanitizer.sanitizeString("../../../etc/passwd", "test_field")
        }
    }

    @Test
    fun `should reject overly long input`() {
        val longInput = "a".repeat(1001)
        assertThrows<ValidationException> {
            sanitizer.sanitizeString(longInput, "test_field")
        }
    }

    @Test
    fun `should handle null input`() {
        val result = sanitizer.sanitizeString(null, "test_field")
        assertEquals(null, result)
    }

    @Test
    fun `should handle empty input`() {
        val result = sanitizer.sanitizeString("", "test_field")
        assertEquals("", result)
    }

    @Test
    fun `should handle whitespace only input`() {
        val result = sanitizer.sanitizeString("   ", "test_field")
        assertEquals("", result)
    }

    @Test
    fun `should sanitize name with normal input`() {
        val result = sanitizer.sanitizeName("John Doe", "user_name")
        assertEquals("John Doe", result)
    }

    @Test
    fun `should reject overly long name`() {
        val longName = "a".repeat(256)
        assertThrows<ValidationException> {
            sanitizer.sanitizeName(longName, "user_name")
        }
    }

    @Test
    fun `should validate valid URL`() {
        val result = sanitizer.sanitizeUrl("https://example.com", "url_field")
        assertEquals("https://example.com", result)
    }

    @Test
    fun `should reject URL with path traversal`() {
        assertThrows<ValidationException> {
            sanitizer.sanitizeUrl("https://example.com/../../../etc/passwd", "url_field")
        }
    }

    @Test
    fun `should reject invalid URL format`() {
        assertThrows<ValidationException> {
            sanitizer.sanitizeUrl("not-a-url", "url_field")
        }
    }

    @Test
    fun `should validate file upload with valid parameters`() {
        val result =
            sanitizer.validateFileUpload(
                "test.jpg",
                1024L,
                setOf("jpg", "png", "gif"),
                1048576L
            )
        assertTrue(result)
    }

    @Test
    fun `should reject file upload with path traversal in filename`() {
        assertThrows<ValidationException> {
            sanitizer.validateFileUpload(
                "../../../etc/passwd",
                1024L,
                setOf("txt"),
                1048576L
            )
        }
    }

    @Test
    fun `should reject file upload with disallowed extension`() {
        assertThrows<ValidationException> {
            sanitizer.validateFileUpload(
                "test.exe",
                1024L,
                setOf("jpg", "png", "gif"),
                1048576L
            )
        }
    }

    @Test
    fun `should reject file upload that is too large`() {
        assertThrows<ValidationException> {
            sanitizer.validateFileUpload(
                "test.jpg",
                // 2MB
                2097152L,
                setOf("jpg", "png", "gif"),
                // 1MB limit
                1048576L
            )
        }
    }

    @Test
    fun `should reject empty filename`() {
        assertThrows<ValidationException> {
            sanitizer.validateFileUpload(
                "",
                1024L,
                setOf("jpg", "png", "gif"),
                1048576L
            )
        }
    }
}

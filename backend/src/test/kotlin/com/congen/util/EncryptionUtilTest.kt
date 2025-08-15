package com.congen.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Base64

/**
 * Unit tests for EncryptionUtil.
 *
 * Tests encryption and decryption functionality for GDPR compliance.
 */
class EncryptionUtilTest {
    private lateinit var encryptionUtil: EncryptionUtil
    private val testKey = "MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI="

    @BeforeEach
    fun setUp() {
        encryptionUtil = EncryptionUtil(testKey)
    }

    @Test
    fun `encrypt should return different values for same input`() {
        val plaintext = "Test User Name"

        val encrypted1 = encryptionUtil.encrypt(plaintext)
        val encrypted2 = encryptionUtil.encrypt(plaintext)

        assertNotNull(encrypted1)
        assertNotNull(encrypted2)
        assertNotEquals(encrypted1, encrypted2, "Encrypted values should be different due to unique IVs")
    }

    @Test
    fun `encrypt and decrypt should preserve original value`() {
        val plaintext = "Test User Name"

        val encrypted = encryptionUtil.encrypt(plaintext)
        val decrypted = encryptionUtil.decrypt(encrypted)

        assertEquals(plaintext, decrypted)
    }

    @Test
    fun `encrypt should handle null input`() {
        val result = encryptionUtil.encrypt(null)
        assertNull(result)
    }

    @Test
    fun `encrypt should handle empty string`() {
        val result = encryptionUtil.encrypt("")
        assertNull(result)
    }

    @Test
    fun `encrypt should handle blank string`() {
        val result = encryptionUtil.encrypt("   ")
        assertNull(result)
    }

    @Test
    fun `decrypt should handle null input`() {
        val result = encryptionUtil.decrypt(null)
        assertNull(result)
    }

    @Test
    fun `decrypt should handle empty string`() {
        val result = encryptionUtil.decrypt("")
        assertNull(result)
    }

    @Test
    fun `decrypt should throw exception for invalid data`() {
        assertThrows(EncryptionException::class.java) {
            encryptionUtil.decrypt("invalid-encrypted-data")
        }
    }

    @Test
    fun `decrypt should throw exception for malformed base64`() {
        assertThrows(EncryptionException::class.java) {
            encryptionUtil.decrypt("not-valid-base64!")
        }
    }

    @Test
    fun `encrypt should handle unicode characters`() {
        val plaintext = "Test User with émojis 🔒 and spëcial çharacters"

        val encrypted = encryptionUtil.encrypt(plaintext)
        val decrypted = encryptionUtil.decrypt(encrypted)

        assertEquals(plaintext, decrypted)
    }

    @Test
    fun `encrypt should handle large text`() {
        val plaintext = "A".repeat(1000) // 1KB text

        val encrypted = encryptionUtil.encrypt(plaintext)
        val decrypted = encryptionUtil.decrypt(encrypted)

        assertEquals(plaintext, decrypted)
    }

    @Test
    fun `generateNewKey should return valid base64 key`() {
        val newKey = encryptionUtil.generateNewKey()

        assertNotNull(newKey)
        // Should be able to decode without exception
        val decodedKey = Base64.getDecoder().decode(newKey)
        assertEquals(32, decodedKey.size, "AES-256 key should be 32 bytes")
    }

    @Test
    fun `generateNewKey should return different keys each time`() {
        val key1 = encryptionUtil.generateNewKey()
        val key2 = encryptionUtil.generateNewKey()

        assertNotEquals(key1, key2, "Generated keys should be unique")
    }

    @Test
    fun `encryption should work with numbers`() {
        val age = "30"
        val height = "175.5"
        val weight = "70.2"

        val encryptedAge = encryptionUtil.encrypt(age)
        val encryptedHeight = encryptionUtil.encrypt(height)
        val encryptedWeight = encryptionUtil.encrypt(weight)

        assertEquals(age, encryptionUtil.decrypt(encryptedAge))
        assertEquals(height, encryptionUtil.decrypt(encryptedHeight))
        assertEquals(weight, encryptionUtil.decrypt(encryptedWeight))
    }
}

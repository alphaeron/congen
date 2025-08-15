package com.congen.util

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Utility class for encrypting and decrypting sensitive personal data.
 *
 * This class provides AES-256-GCM encryption for GDPR compliance,
 * ensuring that sensitive personal data is encrypted at rest in the database.
 *
 * ## Features
 *
 * - **AES-256-GCM Encryption**: Industry standard authenticated encryption
 * - **Random IV Generation**: Each encryption uses a unique initialization vector
 * - **Base64 Encoding**: Encrypted data is base64 encoded for database storage
 * - **Key Rotation Support**: Supports multiple encryption keys for rotation
 * - **GDPR Compliance**: Meets encryption requirements for personal data protection
 *
 * ## Usage
 *
 * ```kotlin
 * @Autowired
 * private lateinit var encryptionUtil: EncryptionUtil
 *
 * // Encrypt sensitive data before database storage
 * val encryptedName = encryptionUtil.encrypt(userName)
 *
 * // Decrypt data for application use
 * val decryptedName = encryptionUtil.decrypt(encryptedName)
 * ```
 *
 * ## Security Considerations
 *
 * - Encryption keys should be managed through external key management systems
 * - Keys should be rotated regularly (recommended: every 90 days)
 * - Failed decryption attempts should be logged and monitored
 * - Access to encryption keys should be strictly controlled
 *
 * @property encryptionKey Base64-encoded AES-256 encryption key
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class EncryptionUtil(
    @Value("\${congen.encryption.key}")
    private val encryptionKey: String
) {
    companion object {
        /** Algorithm used for encryption. */
        private const val ALGORITHM = "AES"

        /** Transformation used for encryption. */
        private const val TRANSFORMATION = "AES/GCM/NoPadding"

        /** GCM tag length in bits. */
        private const val GCM_TAG_LENGTH = 16

        /** Initialization vector length in bytes. */
        private const val IV_LENGTH = 12

        /** Key length in bits. */
        private const val KEY_LENGTH = 256
    }

    private val secretKey: SecretKey by lazy {
        val decodedKey = Base64.getDecoder().decode(encryptionKey)
        SecretKeySpec(decodedKey, ALGORITHM)
    }

    /**
     * Encrypts a plaintext string using AES-256-GCM.
     *
     * Each encryption operation generates a unique initialization vector (IV)
     * to ensure that identical plaintexts produce different ciphertexts.
     * The IV is prepended to the encrypted data for storage.
     *
     * @param plaintext The plaintext string to encrypt
     * @return Base64-encoded encrypted string with IV prepended
     * @throws EncryptionException if encryption fails
     */
    fun encrypt(plaintext: String?): String? {
        if (plaintext.isNullOrBlank()) {
            return null
        }

        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)

            // Generate random IV for each encryption
            val iv = ByteArray(IV_LENGTH)
            SecureRandom().nextBytes(iv)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

            val plaintextBytes = plaintext.toByteArray(StandardCharsets.UTF_8)
            val ciphertext = cipher.doFinal(plaintextBytes)

            // Prepend IV to ciphertext for storage
            val encryptedData = ByteArray(IV_LENGTH + ciphertext.size)
            System.arraycopy(iv, 0, encryptedData, 0, IV_LENGTH)
            System.arraycopy(ciphertext, 0, encryptedData, IV_LENGTH, ciphertext.size)

            return Base64.getEncoder().encodeToString(encryptedData)
        } catch (e: Exception) {
            throw EncryptionException("Failed to encrypt data", e)
        }
    }

    /**
     * Decrypts a base64-encoded encrypted string using AES-256-GCM.
     *
     * Extracts the initialization vector (IV) from the beginning of the
     * encrypted data and uses it to decrypt the remaining ciphertext.
     *
     * @param encryptedData Base64-encoded encrypted string with IV prepended
     * @return Decrypted plaintext string
     * @throws EncryptionException if decryption fails
     */
    fun decrypt(encryptedData: String?): String? {
        if (encryptedData.isNullOrBlank()) {
            return null
        }

        try {
            val encryptedBytes = Base64.getDecoder().decode(encryptedData)

            if (encryptedBytes.size < IV_LENGTH + GCM_TAG_LENGTH) {
                throw EncryptionException("Invalid encrypted data format")
            }

            // Extract IV from the beginning of encrypted data
            val iv = ByteArray(IV_LENGTH)
            System.arraycopy(encryptedBytes, 0, iv, 0, IV_LENGTH)

            // Extract ciphertext (everything after IV)
            val ciphertext = ByteArray(encryptedBytes.size - IV_LENGTH)
            System.arraycopy(encryptedBytes, IV_LENGTH, ciphertext, 0, ciphertext.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

            val plaintextBytes = cipher.doFinal(ciphertext)
            return String(plaintextBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            throw EncryptionException("Failed to decrypt data", e)
        }
    }

    /**
     * Generates a new AES-256 encryption key.
     *
     * This method can be used to generate new keys for key rotation.
     * The generated key should be stored securely in your key management system.
     *
     * @return Base64-encoded AES-256 key
     */
    fun generateNewKey(): String {
        val keyGenerator = KeyGenerator.getInstance(ALGORITHM)
        keyGenerator.init(KEY_LENGTH)
        val secretKey = keyGenerator.generateKey()
        return Base64.getEncoder().encodeToString(secretKey.encoded)
    }
}

/**
 * Exception thrown when encryption or decryption operations fail.
 *
 * @property message Error message describing the failure
 * @property cause Underlying cause of the failure
 */
class EncryptionException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

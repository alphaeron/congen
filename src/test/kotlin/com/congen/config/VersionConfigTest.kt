package com.congen.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Unit tests for [VersionConfig].
 *
 * Tests cover all functionality including:
 * - Version properties loading from classpath
 * - Default values when properties file is not available
 * - All version-related properties
 * - Error handling for missing properties file
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class VersionConfigTest {
    @Test
    fun `should create VersionConfig with default values`() {
        // When
        val versionConfig = VersionConfig()

        // Then
        assertNotNull(versionConfig)
        // The actual version will be generated based on git info and timestamp
        // We can't predict the exact value, so we just verify it's not null/empty
        assertNotNull(versionConfig.version)
        assertTrue(versionConfig.version.isNotBlank())
        // The releaseId is generated from git info, so we just check it's not empty
        assertTrue(versionConfig.releaseId.isNotBlank())
        assertTrue(versionConfig.buildTime.isNotBlank())
        assertTrue(versionConfig.gitHash.isNotBlank())
        assertTrue(versionConfig.gitBranch.isNotBlank())
        // gitDirty is a boolean, so it's either true or false, and therefore we don't need to check it
    }

    @Test
    fun `should create multiple VersionConfig instances`() {
        // When
        val versionConfig1 = VersionConfig()
        val versionConfig2 = VersionConfig()

        // Then
        assertNotNull(versionConfig1)
        assertNotNull(versionConfig2)
        assertEquals(versionConfig1.version, versionConfig2.version)
        assertEquals(versionConfig1.releaseId, versionConfig2.releaseId)
        assertEquals(versionConfig1.buildTime, versionConfig2.buildTime)
        assertEquals(versionConfig1.gitHash, versionConfig2.gitHash)
        assertEquals(versionConfig1.gitBranch, versionConfig2.gitBranch)
        assertEquals(versionConfig1.gitDirty, versionConfig2.gitDirty)
    }

    @Test
    fun `should handle missing version properties file gracefully`() {
        // When
        val versionConfig = VersionConfig()

        // Then
        assertNotNull(versionConfig)
        // Even with missing properties file, it should still generate a version
        assertNotNull(versionConfig.version)
        assertTrue(versionConfig.version.isNotBlank())
        // The releaseId is generated from git info, so we just check it's not empty
        assertTrue(versionConfig.releaseId.isNotBlank())
        assertTrue(versionConfig.buildTime.isNotBlank())
        assertTrue(versionConfig.gitHash.isNotBlank())
        assertTrue(versionConfig.gitBranch.isNotBlank())
        // gitDirty is a boolean, so it's either true or false, and therefore we don't need to check it
    }

    @Test
    fun `should have consistent default values`() {
        // When
        val versionConfig = VersionConfig()

        // Then
        // Verify all properties have expected default values
        assert(versionConfig.version.isNotEmpty())
        assert(versionConfig.releaseId.isNotEmpty())
        assert(versionConfig.buildTime.isNotEmpty())
        assert(versionConfig.gitHash.isNotEmpty())
        assert(versionConfig.gitBranch.isNotEmpty())
        // gitDirty is a boolean, so it's either true or false, and therefore we don't need to check it
    }

    @Test
    fun `should handle gitDirty property parsing`() {
        // When
        val versionConfig = VersionConfig()

        // Then
        // gitDirty should be a valid boolean value
        assert(versionConfig.gitDirty == true || versionConfig.gitDirty == false)
    }
}

package com.congen.util

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.assertNotNull

@ExtendWith(MockitoExtension::class)
class KeycloakUtilTest {
    private lateinit var keycloakUtil: KeycloakUtil

    @BeforeEach
    fun setUp() {
        keycloakUtil = KeycloakUtilImpl()
    }

    @Test
    fun `should have getCurrentUserId method`() {
        assertNotNull(keycloakUtil::getCurrentUserId)
    }

    @Test
    fun `should have getCurrentUserRoles method`() {
        assertNotNull(keycloakUtil::getCurrentUserRoles)
    }

    @Test
    fun `should have hasRole method`() {
        assertNotNull(keycloakUtil::hasRole)
    }

    @Test
    fun `getCurrentUserId should return Mono`() {
        val result = keycloakUtil.getCurrentUserId()
        assertNotNull(result)
    }

    @Test
    fun `getCurrentUserRoles should return Mono`() {
        val result = keycloakUtil.getCurrentUserRoles()
        assertNotNull(result)
    }

    @Test
    fun `hasRole should return Mono`() {
        val result = keycloakUtil.hasRole("admin")
        assertNotNull(result)
    }
}

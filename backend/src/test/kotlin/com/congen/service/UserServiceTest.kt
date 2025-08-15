package com.congen.service

import com.congen.client.KeycloakClient
import com.congen.dal.UserDAL
import com.congen.exceptions.ValidationException
import com.congen.model.User
import com.congen.util.KeycloakUtil
import com.congen.util.UnitConverter
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class UserServiceTest {
    private lateinit var userDAL: UserDAL
    private lateinit var unitConverter: UnitConverter
    private lateinit var keycloakClient: KeycloakClient
    private lateinit var keycloakUtil: KeycloakUtil
    private lateinit var userService: UserService

    private val now = Instant.now()
    private val testUser =
        User(
            keycloakId = "test-keycloak-id",
            name = "Test User",
            createdAt = now,
            updatedAt = now
        )

    @BeforeEach
    fun setUp() {
        userDAL = mock()
        unitConverter = mock()
        keycloakClient = mock()
        keycloakUtil = mock()
        userService = UserService(userDAL, unitConverter, keycloakClient, keycloakUtil)
    }

    @Test
    fun `createUser should create user from Keycloak information successfully`() {
        // Given
        val keycloakId = "test-keycloak-id"
        val name = "Test User"
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(keycloakId))
        whenever(keycloakUtil.getCurrentUserName()).thenReturn(Mono.just(name))
        whenever(userDAL.insertUser(eq(keycloakId), eq(name))).thenReturn(Mono.just(testUser))

        // When
        val result = userService.createUser()

        // Then
        StepVerifier.create(result)
            .expectNext(testUser)
            .verifyComplete()
        verify(keycloakUtil).getCurrentUserId()
        verify(keycloakUtil).getCurrentUserName()
        verify(userDAL).insertUser(keycloakId, name)
    }

    @Test
    fun `createUser should throw ValidationException when name is not available`() {
        // Given
        val keycloakId = "test-keycloak-id"
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(keycloakId))
        whenever(keycloakUtil.getCurrentUserName()).thenReturn(Mono.empty())

        // When & Then
        assertThrows<ValidationException> {
            userService.createUser().block()
        }
        verify(keycloakUtil).getCurrentUserId()
        verify(keycloakUtil).getCurrentUserName()
    }

    @Test
    fun `createUser should throw ValidationException when name is blank`() {
        // Given
        val keycloakId = "test-keycloak-id"
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(keycloakId))
        whenever(keycloakUtil.getCurrentUserName()).thenReturn(Mono.just(""))

        // When & Then
        assertThrows<ValidationException> {
            userService.createUser().block()
        }
        verify(keycloakUtil).getCurrentUserId()
        verify(keycloakUtil).getCurrentUserName()
    }

    @Test
    fun `getUserByKeycloakId should return user successfully`() {
        // Given
        val keycloakId = "test-keycloak-id"
        whenever(userDAL.selectUserByKeycloakId(keycloakId)).thenReturn(Mono.just(testUser))

        // When
        val result = userService.getUserByKeycloakId(keycloakId)

        // Then
        StepVerifier.create(result)
            .expectNext(testUser)
            .verifyComplete()
        verify(userDAL).selectUserByKeycloakId(keycloakId)
    }
}

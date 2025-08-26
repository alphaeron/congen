package com.congen.service

import com.congen.client.KeycloakClient
import com.congen.dal.UserDAL
import com.congen.dal.UserProgramPreferencesDAL
import com.congen.exceptions.ValidationException
import com.congen.model.User
import com.congen.model.UserConsent
import com.congen.model.UserProgramPreferences
import com.congen.util.KeycloakUtil
import com.congen.util.UnitConverter
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
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
    private lateinit var userProgramPreferencesDAL: UserProgramPreferencesDAL
    private lateinit var unitConverter: UnitConverter
    private lateinit var keycloakClient: KeycloakClient
    private lateinit var keycloakUtil: KeycloakUtil
    private lateinit var gdprComplianceService: GdprComplianceService
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
        userProgramPreferencesDAL = mock()
        unitConverter = mock()
        keycloakClient = mock()
        keycloakUtil = mock()
        gdprComplianceService = mock()
        userService = UserService(userDAL, userProgramPreferencesDAL, unitConverter, keycloakClient, keycloakUtil, gdprComplianceService)
    }

    @Test
    fun `createUser should create user from Keycloak information successfully`() {
        val keycloakId = "test-keycloak-id"
        val name = "Test User"
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(keycloakId))
        whenever(keycloakUtil.getCurrentUserName()).thenReturn(Mono.just(name))
        whenever(userDAL.insertUser(eq(keycloakId), eq(name))).thenReturn(Mono.just(testUser))
        whenever(gdprComplianceService.updateUserConsent(eq(keycloakId), eq(true))).thenReturn(
            Mono.just(
                UserConsent(
                    keycloakId = keycloakId,
                    dataProcessingConsent = true,
                    consentTimestamp = now,
                    createdAt = now,
                    updatedAt = now
                )
            )
        )
        whenever(userProgramPreferencesDAL.insertUserProgramPreferences(any(), any(), any()))
            .thenReturn(
                Mono.just(
                    UserProgramPreferences(
                        userId = keycloakId,
                        programDaysPerWeek = 4,
                        sessionTimeLengthInMinutes = 60,
                        createdAt = now,
                        updatedAt = now
                    )
                )
            )

        val result = userService.insertUser()

        StepVerifier.create(result)
            .expectNext(testUser)
            .verifyComplete()
        verify(keycloakUtil).getCurrentUserId()
        verify(keycloakUtil).getCurrentUserName()
        verify(userDAL).insertUser(keycloakId, name)
        verify(gdprComplianceService).updateUserConsent(keycloakId, true)
        verify(userProgramPreferencesDAL).insertUserProgramPreferences(any(), any(), any())
    }

    @Test
    fun `createUser should throw ValidationException when name is not available`() {
        val keycloakId = "test-keycloak-id"
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(keycloakId))
        whenever(keycloakUtil.getCurrentUserName()).thenReturn(Mono.empty())

        assertThrows<ValidationException> {
            userService.insertUser().block()
        }
        verify(keycloakUtil).getCurrentUserId()
        verify(keycloakUtil).getCurrentUserName()
    }

    @Test
    fun `createUser should throw ValidationException when name is blank`() {
        val keycloakId = "test-keycloak-id"
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(keycloakId))
        whenever(keycloakUtil.getCurrentUserName()).thenReturn(Mono.just(""))

        assertThrows<ValidationException> {
            userService.insertUser().block()
        }
        verify(keycloakUtil).getCurrentUserId()
        verify(keycloakUtil).getCurrentUserName()
    }

    @Test
    fun `getUserByKeycloakId should return user successfully`() {
        val keycloakId = "test-keycloak-id"
        whenever(userDAL.selectUserByKeycloakId(keycloakId)).thenReturn(Mono.just(testUser))

        val result = userService.selectUserByKeycloakId(keycloakId)

        StepVerifier.create(result)
            .expectNext(testUser)
            .verifyComplete()
        verify(userDAL).selectUserByKeycloakId(keycloakId)
    }
}

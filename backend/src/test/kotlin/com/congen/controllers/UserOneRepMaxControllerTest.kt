package com.congen.controllers

import com.congen.createGdprComplianceServiceSpy
import com.congen.dal.UserOneRepMaxDAL
import com.congen.exceptions.DatabaseException
import com.congen.model.UserOneRepMax
import com.congen.service.GdprComplianceService
import com.congen.service.UserOneRepMaxService
import com.congen.util.KeycloakUtil
import com.congen.util.ValidationUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import org.springframework.test.context.TestPropertySource
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.Instant

/**
 * Unit tests for UserOneRepMaxController.
 *
 * These tests verify the REST API endpoints for user one rep max operations,
 * including CRUD operations and error handling.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension::class)
@TestPropertySource(
    properties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"]
)
class UserOneRepMaxControllerTest {
    private lateinit var userOneRepMaxDAL: UserOneRepMaxDAL
    private lateinit var userOneRepMaxService: UserOneRepMaxService
    private lateinit var validationUtil: ValidationUtil
    private lateinit var keycloakUtil: KeycloakUtil
    private lateinit var gdprComplianceService: GdprComplianceService
    private lateinit var userOneRepMaxController: UserOneRepMaxController

    companion object {
        private const val TEST_USER_ID = "test-user-id"
        private const val TEST_EXERCISE_NAME = "Bench Press"

        private val TEST_ONE_REP_MAX =
            UserOneRepMax(
                userId = TEST_USER_ID,
                exerciseName = TEST_EXERCISE_NAME,
                oneRepMax = BigDecimal("100.0"),
                updatedAt = Instant.now()
            )
    }

    @BeforeEach
    fun setUp() {
        userOneRepMaxDAL = mock()
        userOneRepMaxService = mock()
        validationUtil = mock()
        keycloakUtil = mock()
        gdprComplianceService = createGdprComplianceServiceSpy()
        userOneRepMaxController =
            UserOneRepMaxController(
                userOneRepMaxDAL,
                userOneRepMaxService,
                validationUtil,
                keycloakUtil,
                gdprComplianceService
            )

        // Mock GDPR compliance service for all tests
        doReturn(Mono.just(true)).whenever(gdprComplianceService).hasUserConsent(any<String>())
    }

    @Test
    fun `getOneRepMaxesByUserId should return list of one rep maxes`() {
        val oneRepMaxes = listOf(TEST_ONE_REP_MAX)
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(TEST_USER_ID))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(setOf("user")))
        whenever(userOneRepMaxService.selectUserOneRepMaxByUser(TEST_USER_ID, null))
            .thenReturn(Mono.just(oneRepMaxes))

        val result = userOneRepMaxController.getOneRepMaxesByUserId(TEST_USER_ID, null)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(oneRepMaxes))
            .verifyComplete()

        verify(userOneRepMaxService).selectUserOneRepMaxByUser(TEST_USER_ID, null)
    }

    @Test
    fun `getOneRepMaxesByUserId should pass unit parameter to service`() {
        val oneRepMaxes = listOf(TEST_ONE_REP_MAX)
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(TEST_USER_ID))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(setOf("user")))
        whenever(userOneRepMaxService.selectUserOneRepMaxByUser(TEST_USER_ID, "lbs"))
            .thenReturn(Mono.just(oneRepMaxes))

        val result = userOneRepMaxController.getOneRepMaxesByUserId(TEST_USER_ID, "lbs")

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(oneRepMaxes))
            .verifyComplete()

        verify(userOneRepMaxService).selectUserOneRepMaxByUser(TEST_USER_ID, "lbs")
    }

    @Test
    fun `getOneRepMaxByUserAndExercise should return one rep max when found`() {
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(TEST_USER_ID))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(setOf("user")))
        whenever(userOneRepMaxService.selectUserOneRepMax(TEST_USER_ID, TEST_EXERCISE_NAME, null))
            .thenReturn(Mono.just(TEST_ONE_REP_MAX))

        val result = userOneRepMaxController.getOneRepMaxByUserAndExercise(TEST_USER_ID, TEST_EXERCISE_NAME, null)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(TEST_ONE_REP_MAX))
            .verifyComplete()

        verify(userOneRepMaxService).selectUserOneRepMax(TEST_USER_ID, TEST_EXERCISE_NAME, null)
    }

    @Test
    fun `getOneRepMaxByUserAndExercise should return not found when not found`() {
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(TEST_USER_ID))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(setOf("user")))
        whenever(userOneRepMaxService.selectUserOneRepMax(TEST_USER_ID, TEST_EXERCISE_NAME, null))
            .thenReturn(Mono.error(DatabaseException("Not found")))

        val result = userOneRepMaxController.getOneRepMaxByUserAndExercise(TEST_USER_ID, TEST_EXERCISE_NAME, null)

        StepVerifier.create(result)
            .expectError(DatabaseException::class.java)
            .verify()

        verify(userOneRepMaxService).selectUserOneRepMax(TEST_USER_ID, TEST_EXERCISE_NAME, null)
    }

    @Test
    fun `getOneRepMaxByUserAndExercise should pass unit parameter to service`() {
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(TEST_USER_ID))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(setOf("user")))
        whenever(userOneRepMaxService.selectUserOneRepMax(TEST_USER_ID, TEST_EXERCISE_NAME, "kg"))
            .thenReturn(Mono.just(TEST_ONE_REP_MAX))

        val result = userOneRepMaxController.getOneRepMaxByUserAndExercise(TEST_USER_ID, TEST_EXERCISE_NAME, "kg")

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(TEST_ONE_REP_MAX))
            .verifyComplete()

        verify(userOneRepMaxService).selectUserOneRepMax(TEST_USER_ID, TEST_EXERCISE_NAME, "kg")
    }

    @Test
    fun `upsertOneRepMax should return created one rep max`() {
        val oneRepMax = BigDecimal("100.0")
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(TEST_USER_ID))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(setOf("user")))
        whenever(userOneRepMaxService.upsertUserOneRepMax(TEST_USER_ID, TEST_EXERCISE_NAME, oneRepMax, "kg"))
            .thenReturn(Mono.just(TEST_ONE_REP_MAX))

        val result = userOneRepMaxController.upsertOneRepMax(TEST_USER_ID, TEST_EXERCISE_NAME, oneRepMax, "kg")

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(TEST_ONE_REP_MAX))
            .verifyComplete()

        verify(userOneRepMaxService).upsertUserOneRepMax(TEST_USER_ID, TEST_EXERCISE_NAME, oneRepMax, "kg")
    }

    @Test
    fun `upsertOneRepMax should pass unit parameter to service`() {
        val oneRepMax = BigDecimal("100.0")
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(TEST_USER_ID))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(setOf("user")))
        whenever(userOneRepMaxService.upsertUserOneRepMax(TEST_USER_ID, TEST_EXERCISE_NAME, oneRepMax, "lbs"))
            .thenReturn(Mono.just(TEST_ONE_REP_MAX))

        val result = userOneRepMaxController.upsertOneRepMax(TEST_USER_ID, TEST_EXERCISE_NAME, oneRepMax, "lbs")

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(TEST_ONE_REP_MAX))
            .verifyComplete()

        verify(userOneRepMaxService).upsertUserOneRepMax(TEST_USER_ID, TEST_EXERCISE_NAME, oneRepMax, "lbs")
    }

    @Test
    fun `deleteOneRepMax should return deleted one rep max when deleted`() {
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(TEST_USER_ID))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(setOf("user")))
        whenever(userOneRepMaxService.deleteUserOneRepMax(TEST_USER_ID, TEST_EXERCISE_NAME))
            .thenReturn(Mono.just(TEST_ONE_REP_MAX))

        val result = userOneRepMaxController.deleteOneRepMax(TEST_USER_ID, TEST_EXERCISE_NAME)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(TEST_ONE_REP_MAX))
            .verifyComplete()

        verify(userOneRepMaxService).deleteUserOneRepMax(TEST_USER_ID, TEST_EXERCISE_NAME)
    }

    @Test
    fun `deleteOneRepMax should return not found when not found`() {
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(TEST_USER_ID))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(setOf("user")))
        whenever(userOneRepMaxService.deleteUserOneRepMax(TEST_USER_ID, TEST_EXERCISE_NAME))
            .thenReturn(Mono.error(DatabaseException("Not found")))

        val result = userOneRepMaxController.deleteOneRepMax(TEST_USER_ID, TEST_EXERCISE_NAME)

        StepVerifier.create(result)
            .expectError(DatabaseException::class.java)
            .verify()

        verify(userOneRepMaxService).deleteUserOneRepMax(TEST_USER_ID, TEST_EXERCISE_NAME)
    }

    @Test
    fun `getOneRepMaxesByUserId should allow admin to access any user data`() {
        val oneRepMaxes = listOf(TEST_ONE_REP_MAX)
        val adminUserId = "admin-user-id"
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(adminUserId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(setOf("admin")))
        whenever(userOneRepMaxService.selectUserOneRepMaxByUser(TEST_USER_ID, null))
            .thenReturn(Mono.just(oneRepMaxes))

        val result = userOneRepMaxController.getOneRepMaxesByUserId(TEST_USER_ID, null)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(oneRepMaxes))
            .verifyComplete()

        verify(userOneRepMaxService).selectUserOneRepMaxByUser(TEST_USER_ID, null)
    }

    @Test
    fun `getOneRepMaxByUserAndExercise should allow admin to access any user data`() {
        val adminUserId = "admin-user-id"
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(adminUserId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(setOf("admin")))
        whenever(userOneRepMaxService.selectUserOneRepMax(TEST_USER_ID, TEST_EXERCISE_NAME, null))
            .thenReturn(Mono.just(TEST_ONE_REP_MAX))

        val result = userOneRepMaxController.getOneRepMaxByUserAndExercise(TEST_USER_ID, TEST_EXERCISE_NAME, null)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(TEST_ONE_REP_MAX))
            .verifyComplete()

        verify(userOneRepMaxService).selectUserOneRepMax(TEST_USER_ID, TEST_EXERCISE_NAME, null)
    }

    @Test
    fun `upsertOneRepMax should allow admin to update any user data`() {
        val oneRepMax = BigDecimal("100.0")
        val adminUserId = "admin-user-id"
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(adminUserId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(setOf("admin")))
        whenever(userOneRepMaxService.upsertUserOneRepMax(TEST_USER_ID, TEST_EXERCISE_NAME, oneRepMax, "kg"))
            .thenReturn(Mono.just(TEST_ONE_REP_MAX))

        val result = userOneRepMaxController.upsertOneRepMax(TEST_USER_ID, TEST_EXERCISE_NAME, oneRepMax, "kg")

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(TEST_ONE_REP_MAX))
            .verifyComplete()

        verify(userOneRepMaxService).upsertUserOneRepMax(TEST_USER_ID, TEST_EXERCISE_NAME, oneRepMax, "kg")
    }

    @Test
    fun `deleteOneRepMax should allow admin to delete any user data`() {
        val adminUserId = "admin-user-id"
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(adminUserId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(setOf("admin")))
        whenever(userOneRepMaxService.deleteUserOneRepMax(TEST_USER_ID, TEST_EXERCISE_NAME))
            .thenReturn(Mono.just(TEST_ONE_REP_MAX))

        val result = userOneRepMaxController.deleteOneRepMax(TEST_USER_ID, TEST_EXERCISE_NAME)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(TEST_ONE_REP_MAX))
            .verifyComplete()

        verify(userOneRepMaxService).deleteUserOneRepMax(TEST_USER_ID, TEST_EXERCISE_NAME)
    }

    @Test
    fun `getOneRepMaxesByUserId should allow service user to access any user data`() {
        val oneRepMaxes = listOf(TEST_ONE_REP_MAX)
        val serviceUserId = "service-user-id"
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(serviceUserId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(setOf("service")))
        whenever(userOneRepMaxService.selectUserOneRepMaxByUser(TEST_USER_ID, null))
            .thenReturn(Mono.just(oneRepMaxes))

        val result = userOneRepMaxController.getOneRepMaxesByUserId(TEST_USER_ID, null)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(oneRepMaxes))
            .verifyComplete()

        verify(userOneRepMaxService).selectUserOneRepMaxByUser(TEST_USER_ID, null)
    }

    @Test
    fun `getOneRepMaxByUserAndExercise should allow service user to access any user data`() {
        val serviceUserId = "service-user-id"
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(serviceUserId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(setOf("service")))
        whenever(userOneRepMaxService.selectUserOneRepMax(TEST_USER_ID, TEST_EXERCISE_NAME, null))
            .thenReturn(Mono.just(TEST_ONE_REP_MAX))

        val result = userOneRepMaxController.getOneRepMaxByUserAndExercise(TEST_USER_ID, TEST_EXERCISE_NAME, null)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(TEST_ONE_REP_MAX))
            .verifyComplete()

        verify(userOneRepMaxService).selectUserOneRepMax(TEST_USER_ID, TEST_EXERCISE_NAME, null)
    }

    @Test
    fun `upsertOneRepMax should allow service user to update any user data`() {
        val oneRepMax = BigDecimal("100.0")
        val serviceUserId = "service-user-id"
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(serviceUserId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(setOf("service")))
        whenever(userOneRepMaxService.upsertUserOneRepMax(TEST_USER_ID, TEST_EXERCISE_NAME, oneRepMax, "kg"))
            .thenReturn(Mono.just(TEST_ONE_REP_MAX))

        val result = userOneRepMaxController.upsertOneRepMax(TEST_USER_ID, TEST_EXERCISE_NAME, oneRepMax, "kg")

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(TEST_ONE_REP_MAX))
            .verifyComplete()

        verify(userOneRepMaxService).upsertUserOneRepMax(TEST_USER_ID, TEST_EXERCISE_NAME, oneRepMax, "kg")
    }

    @Test
    fun `deleteOneRepMax should allow service user to delete any user data`() {
        val serviceUserId = "service-user-id"
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(serviceUserId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(setOf("service")))
        whenever(userOneRepMaxService.deleteUserOneRepMax(TEST_USER_ID, TEST_EXERCISE_NAME))
            .thenReturn(Mono.just(TEST_ONE_REP_MAX))

        val result = userOneRepMaxController.deleteOneRepMax(TEST_USER_ID, TEST_EXERCISE_NAME)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(TEST_ONE_REP_MAX))
            .verifyComplete()

        verify(userOneRepMaxService).deleteUserOneRepMax(TEST_USER_ID, TEST_EXERCISE_NAME)
    }
}

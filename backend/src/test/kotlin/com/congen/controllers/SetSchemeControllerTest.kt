package com.congen.controllers

import com.congen.createGdprComplianceServiceSpy
import com.congen.mockSetScheme
import com.congen.model.SetScheme
import com.congen.service.GdprComplianceService
import com.congen.service.ProgrammedExerciseService
import com.congen.service.SetSchemeService
import com.congen.util.KeycloakUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.Instant

/**
 * Unit tests for SetSchemeController.
 *
 * These tests verify the REST API endpoints for set scheme operations,
 * including CRUD operations and error handling.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class SetSchemeControllerTest {
    @Mock
    private lateinit var setSchemeService: SetSchemeService

    @Mock
    private lateinit var keycloakUtil: KeycloakUtil

    @Mock
    private lateinit var programmedExerciseService: ProgrammedExerciseService

    private lateinit var gdprComplianceService: GdprComplianceService

    private lateinit var setSchemeController: SetSchemeController

    companion object {
        private const val SCHEME_ID_1 = 1L
        private const val SCHEME_ID_2 = 2L
        private const val PROGRAMMED_EXERCISE_ID = 5L
        private const val SET_NUMBER_1 = 1
        private const val SET_NUMBER_2 = 2
        private const val TARGET_REP_COUNT = 5
        private const val REST_SECONDS_90 = 90
        private const val REST_SECONDS_120 = 120
        private const val NON_EXISTENT_ID = 999L
        private const val TARGET_WEIGHT_100 = "100.0"
        private const val TARGET_WEIGHT_110 = "110.0"
    }

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        gdprComplianceService = createGdprComplianceServiceSpy()
        setSchemeController = SetSchemeController(setSchemeService, keycloakUtil, programmedExerciseService, gdprComplianceService)

        // Mock KeycloakUtil methods for all tests
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just("test-keycloak-user-id"))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(setOf("user")))
        
        // Mock GDPR compliance service for all tests
        doReturn(Mono.just(true)).whenever(gdprComplianceService).hasUserConsent(any<String>())
    }

    @Test
    fun `should get all set schemes for admin user`() {
        val now = Instant.now()
        val setSchemes =
            listOf(
                mockSetScheme(
                    id = SCHEME_ID_1,
                    programmedExerciseId = PROGRAMMED_EXERCISE_ID,
                    setNumber = SET_NUMBER_1,
                    targetWeight = BigDecimal(TARGET_WEIGHT_100),
                    targetRepCount = TARGET_REP_COUNT,
                    restSeconds = REST_SECONDS_90,
                    createdAt = now,
                    updatedAt = now
                ),
                mockSetScheme(
                    id = SCHEME_ID_2,
                    programmedExerciseId = PROGRAMMED_EXERCISE_ID,
                    setNumber = SET_NUMBER_2,
                    isAmrap = true,
                    targetWeight = BigDecimal(TARGET_WEIGHT_100),
                    restSeconds = REST_SECONDS_90,
                    createdAt = now,
                    updatedAt = now
                )
            )
        val userId = "123"
        val roles = setOf("admin")

        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(setSchemeService.selectSetSchemes()).thenReturn(Mono.just(setSchemes))

        val result = setSchemeController.getAll()
        StepVerifier.create(result)
            .assertNext { resp ->
                assert(resp.statusCode == HttpStatus.OK)
                assert(resp.body == setSchemes)
            }
            .verifyComplete()
        verify(setSchemeService).selectSetSchemes()
    }

    @Test
    fun `should get user owned set schemes for regular user`() {
        val now = Instant.now()
        val userSetSchemes =
            listOf(
                mockSetScheme(
                    id = SCHEME_ID_1,
                    programmedExerciseId = PROGRAMMED_EXERCISE_ID,
                    setNumber = SET_NUMBER_1,
                    targetWeight = BigDecimal(TARGET_WEIGHT_100),
                    targetRepCount = TARGET_REP_COUNT,
                    restSeconds = REST_SECONDS_90,
                    createdAt = now,
                    updatedAt = now
                )
            )
        val userId = "123"
        val roles = setOf("user")

        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(setSchemeService.selectSetSchemesByUserId(userId)).thenReturn(Mono.just(userSetSchemes))

        val result = setSchemeController.getAll()
        StepVerifier.create(result)
            .assertNext { resp ->
                assert(resp.statusCode == HttpStatus.OK)
                assert(resp.body == userSetSchemes)
            }
            .verifyComplete()
        verify(setSchemeService).selectSetSchemesByUserId(userId)
    }

    @Test
    fun `should get all set schemes for service user`() {
        val now = Instant.now()
        val allSetSchemes =
            listOf(
                mockSetScheme(
                    id = SCHEME_ID_1,
                    programmedExerciseId = PROGRAMMED_EXERCISE_ID,
                    setNumber = SET_NUMBER_1,
                    targetWeight = BigDecimal(TARGET_WEIGHT_100),
                    targetRepCount = TARGET_REP_COUNT,
                    restSeconds = REST_SECONDS_90,
                    createdAt = now,
                    updatedAt = now
                ),
                mockSetScheme(
                    id = SCHEME_ID_2,
                    programmedExerciseId = PROGRAMMED_EXERCISE_ID,
                    setNumber = SET_NUMBER_2,
                    isAmrap = true,
                    targetWeight = BigDecimal(TARGET_WEIGHT_100),
                    restSeconds = REST_SECONDS_90,
                    createdAt = now,
                    updatedAt = now
                )
            )
        val userId = "123"
        val roles = setOf("service")

        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(setSchemeService.selectSetSchemes()).thenReturn(Mono.just(allSetSchemes))

        val result = setSchemeController.getAll()
        StepVerifier.create(result)
            .assertNext { resp ->
                assert(resp.statusCode == HttpStatus.OK)
                assert(resp.body == allSetSchemes)
            }
            .verifyComplete()
        verify(setSchemeService).selectSetSchemes()
    }

    @Test
    fun `should return empty list when regular user has no owned set schemes`() {
        val emptyList = emptyList<SetScheme>()
        val userId = "123"
        val roles = setOf("user")

        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(setSchemeService.selectSetSchemesByUserId(userId)).thenReturn(Mono.just(emptyList))

        val result = setSchemeController.getAll()
        StepVerifier.create(result)
            .assertNext { resp ->
                assert(resp.statusCode == HttpStatus.OK)
                assert(resp.body == emptyList)
            }
            .verifyComplete()
        verify(setSchemeService).selectSetSchemesByUserId(userId)
    }

    @Test
    fun `should propagate errors from getAll`() {
        val userId = "123"
        val roles = setOf("user")
        val databaseError = RuntimeException("Database error")

        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(setSchemeService.selectSetSchemesByUserId(userId)).thenReturn(Mono.error(databaseError))

        val result = setSchemeController.getAll()
        StepVerifier.create(result)
            .expectError(databaseError::class.java)
            .verify()
        verify(setSchemeService).selectSetSchemesByUserId(userId)
    }

    @Test
    fun `should get set scheme by id`() {
        val now = Instant.now()
        val setScheme =
            mockSetScheme(
                id = SCHEME_ID_1,
                programmedExerciseId = PROGRAMMED_EXERCISE_ID,
                setNumber = SET_NUMBER_1,
                targetWeight = BigDecimal(TARGET_WEIGHT_100),
                targetRepCount = TARGET_REP_COUNT,
                restSeconds = REST_SECONDS_90,
                createdAt = now,
                updatedAt = now
            )
        whenever(setSchemeService.isOwner(SCHEME_ID_1, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(setSchemeService.selectSetSchemeById(SCHEME_ID_1)).thenReturn(Mono.just(setScheme))
        val result = setSchemeController.get(SCHEME_ID_1)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(setScheme))
            .verifyComplete()
    }

    @Test
    fun `should return empty when set scheme not found`() {
        whenever(setSchemeService.isOwner(NON_EXISTENT_ID, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(setSchemeService.selectSetSchemeById(NON_EXISTENT_ID)).thenReturn(Mono.empty())
        val result = setSchemeController.get(NON_EXISTENT_ID)
        StepVerifier.create(result)
            .expectComplete()
            .verify()
    }

    @Test
    fun `should create set scheme`() {
        val now = Instant.now()
        val setScheme =
            mockSetScheme(
                id = SCHEME_ID_1,
                programmedExerciseId = PROGRAMMED_EXERCISE_ID,
                setNumber = SET_NUMBER_1,
                targetWeight = BigDecimal(TARGET_WEIGHT_100),
                targetRepCount = TARGET_REP_COUNT,
                restSeconds = REST_SECONDS_90,
                createdAt = now,
                updatedAt = now
            )
        whenever(programmedExerciseService.isOwner(PROGRAMMED_EXERCISE_ID, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(
            setSchemeService.createSetScheme(
                PROGRAMMED_EXERCISE_ID,
                SET_NUMBER_1,
                false,
                false,
                false,
                null,
                null,
                null,
                TARGET_WEIGHT_100,
                null,
                TARGET_REP_COUNT,
                null,
                REST_SECONDS_90,
                "KG"
            )
        ).thenReturn(Mono.just(setScheme))
        val result =
            setSchemeController.save(
                PROGRAMMED_EXERCISE_ID,
                SET_NUMBER_1,
                false,
                false,
                false,
                null,
                null,
                null,
                TARGET_WEIGHT_100,
                null,
                TARGET_REP_COUNT,
                null,
                REST_SECONDS_90,
                "KG"
            )
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(setScheme))
            .verifyComplete()
    }

    @Test
    fun `should update set scheme`() {
        val now = Instant.now()
        val setScheme =
            mockSetScheme(
                id = SCHEME_ID_1,
                programmedExerciseId = PROGRAMMED_EXERCISE_ID,
                setNumber = SET_NUMBER_2,
                isAmrap = true,
                targetWeight = BigDecimal(TARGET_WEIGHT_110),
                restSeconds = REST_SECONDS_120,
                createdAt = now,
                updatedAt = now
            )
        whenever(setSchemeService.isOwner(SCHEME_ID_1, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(programmedExerciseService.isOwner(PROGRAMMED_EXERCISE_ID, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(
            setSchemeService.updateSetSchemeWithUnit(
                SCHEME_ID_1,
                PROGRAMMED_EXERCISE_ID,
                SET_NUMBER_2,
                true,
                false,
                false,
                null,
                null,
                null,
                TARGET_WEIGHT_110,
                null,
                null,
                null,
                REST_SECONDS_120,
                "KG"
            )
        ).thenReturn(Mono.just(setScheme))
        val result =
            setSchemeController.update(
                SCHEME_ID_1,
                PROGRAMMED_EXERCISE_ID,
                SET_NUMBER_2,
                true,
                false,
                false,
                null,
                null,
                null,
                TARGET_WEIGHT_110,
                null,
                null,
                null,
                REST_SECONDS_120,
                "KG"
            )
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(setScheme))
            .verifyComplete()
    }

    @Test
    fun `should delete set scheme`() {
        val now = Instant.now()
        val setScheme =
            mockSetScheme(
                id = SCHEME_ID_1,
                programmedExerciseId = PROGRAMMED_EXERCISE_ID,
                setNumber = SET_NUMBER_1,
                targetWeight = BigDecimal(TARGET_WEIGHT_100),
                targetRepCount = TARGET_REP_COUNT,
                restSeconds = REST_SECONDS_90,
                createdAt = now,
                updatedAt = now
            )
        whenever(setSchemeService.isOwner(SCHEME_ID_1, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(setSchemeService.deleteSetScheme(SCHEME_ID_1)).thenReturn(Mono.just(setScheme))
        val result = setSchemeController.delete(SCHEME_ID_1)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(setScheme))
            .verifyComplete()
    }

    @Test
    fun `should get set schemes by programmed exercise id`() {
        val now = Instant.now()
        val setSchemes =
            listOf(
                mockSetScheme(
                    id = SCHEME_ID_1,
                    programmedExerciseId = PROGRAMMED_EXERCISE_ID,
                    setNumber = SET_NUMBER_1,
                    targetWeight = BigDecimal(TARGET_WEIGHT_100),
                    targetRepCount = TARGET_REP_COUNT,
                    restSeconds = REST_SECONDS_90,
                    createdAt = now,
                    updatedAt = now
                ),
                mockSetScheme(
                    id = SCHEME_ID_2,
                    programmedExerciseId = PROGRAMMED_EXERCISE_ID,
                    setNumber = SET_NUMBER_2,
                    isAmrap = true,
                    targetWeight = BigDecimal(TARGET_WEIGHT_100),
                    restSeconds = REST_SECONDS_90,
                    createdAt = now,
                    updatedAt = now
                )
            )
        whenever(programmedExerciseService.isOwner(PROGRAMMED_EXERCISE_ID, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(setSchemeService.selectSetSchemesByProgrammedExerciseId(PROGRAMMED_EXERCISE_ID)).thenReturn(Mono.just(setSchemes))
        val result = setSchemeController.getByProgrammedExerciseId(PROGRAMMED_EXERCISE_ID)
        StepVerifier.create(result)
            .assertNext { resp ->
                assert(resp.statusCode == HttpStatus.OK)
                assert(resp.body == setSchemes)
            }
            .verifyComplete()
    }

    @Test
    fun `should create set scheme with lbs unit`() {
        val now = Instant.now()
        val setScheme =
            mockSetScheme(
                id = SCHEME_ID_1,
                programmedExerciseId = PROGRAMMED_EXERCISE_ID,
                setNumber = SET_NUMBER_1,
                // 100 lbs in kg
                targetWeight = BigDecimal("45.36"),
                targetRepCount = TARGET_REP_COUNT,
                restSeconds = REST_SECONDS_90,
                createdAt = now,
                updatedAt = now
            )
        whenever(programmedExerciseService.isOwner(PROGRAMMED_EXERCISE_ID, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(
            setSchemeService.createSetScheme(
                PROGRAMMED_EXERCISE_ID,
                SET_NUMBER_1,
                false,
                false,
                false,
                null,
                null,
                null,
                "100.0",
                null,
                TARGET_REP_COUNT,
                null,
                REST_SECONDS_90,
                "LBS"
            )
        ).thenReturn(Mono.just(setScheme))
        val result =
            setSchemeController.save(
                PROGRAMMED_EXERCISE_ID,
                SET_NUMBER_1,
                false,
                false,
                false,
                null,
                null,
                null,
                "100.0",
                null,
                TARGET_REP_COUNT,
                null,
                REST_SECONDS_90,
                "LBS"
            )
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(setScheme))
            .verifyComplete()
    }

    @Test
    fun `should update set scheme with lbs unit`() {
        val now = Instant.now()
        val setScheme =
            mockSetScheme(
                id = SCHEME_ID_1,
                programmedExerciseId = PROGRAMMED_EXERCISE_ID,
                setNumber = SET_NUMBER_2,
                isAmrap = true,
                // 110 lbs in kg
                targetWeight = BigDecimal("49.90"),
                restSeconds = REST_SECONDS_120,
                createdAt = now,
                updatedAt = now
            )
        whenever(setSchemeService.isOwner(SCHEME_ID_1, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(programmedExerciseService.isOwner(PROGRAMMED_EXERCISE_ID, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(
            setSchemeService.updateSetSchemeWithUnit(
                SCHEME_ID_1,
                PROGRAMMED_EXERCISE_ID,
                SET_NUMBER_2,
                true,
                false,
                false,
                null,
                null,
                null,
                "110.0",
                null,
                null,
                null,
                REST_SECONDS_120,
                "LBS"
            )
        ).thenReturn(Mono.just(setScheme))
        val result =
            setSchemeController.update(
                SCHEME_ID_1,
                PROGRAMMED_EXERCISE_ID,
                SET_NUMBER_2,
                true,
                false,
                false,
                null,
                null,
                null,
                "110.0",
                null,
                null,
                null,
                REST_SECONDS_120,
                "LBS"
            )
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(setScheme))
            .verifyComplete()
    }
}

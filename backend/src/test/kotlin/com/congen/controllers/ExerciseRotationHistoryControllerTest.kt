package com.congen.controllers

import com.congen.dal.ExerciseRotationHistoryDAL
import com.congen.exceptions.DatabaseException
import com.congen.model.ExerciseRotationHistory
import com.congen.service.ExerciseRotationHistoryService
import com.congen.util.KeycloakUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant

/**
 * Unit tests for ExerciseRotationHistoryController.
 *
 * These tests verify the REST API endpoints for exercise rotation history operations,
 * including CRUD operations and error handling.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class ExerciseRotationHistoryControllerTest {
    private lateinit var exerciseRotationHistoryDAL: ExerciseRotationHistoryDAL
    private lateinit var exerciseRotationHistoryService: ExerciseRotationHistoryService
    private lateinit var keycloakUtil: KeycloakUtil
    private lateinit var exerciseRotationHistoryController: ExerciseRotationHistoryController

    companion object {
        private const val ID_1 = 1L
        private const val ID_2 = 2L
        private const val USER_ID = "1"
        private const val BENCH_PRESS = "Bench Press"
        private const val SQUAT = "Back Squat"
        private const val BARBELL_BENCH_PRESS = "Bench Press"
        private const val NON_EXISTENT_ID = 999L
    }

    @BeforeEach
    fun setUp() {
        exerciseRotationHistoryDAL = mock()
        exerciseRotationHistoryService = mock()
        keycloakUtil = mock()
        exerciseRotationHistoryController = ExerciseRotationHistoryController(exerciseRotationHistoryService, keycloakUtil)

        // Mock KeycloakUtil methods for all tests
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(USER_ID))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(setOf("user")))
    }

    @Test
    fun `should get all exercise rotation histories`() {
        val now = Instant.now()
        val exerciseRotationHistories =
            listOf(
                ExerciseRotationHistory(
                    id = ID_1,
                    userId = USER_ID,
                    exerciseName = BENCH_PRESS,
                    isAccessory = false,
                    createdAt = now
                ),
                ExerciseRotationHistory(
                    id = ID_2,
                    userId = USER_ID,
                    exerciseName = SQUAT,
                    isAccessory = true,
                    createdAt = now
                )
            )
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just("1"))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(setOf("admin")))
        whenever(exerciseRotationHistoryService.selectAll()).thenReturn(Mono.just(exerciseRotationHistories))
        val result = exerciseRotationHistoryController.getAll()
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseRotationHistories))
            .verifyComplete()
    }

    @Test
    fun `should get exercise rotation history by id`() {
        val now = Instant.now()
        val exerciseRotationHistory =
            ExerciseRotationHistory(
                id = ID_1,
                userId = USER_ID,
                exerciseName = BENCH_PRESS,
                isAccessory = false,
                createdAt = now
            )
        whenever(exerciseRotationHistoryService.selectById(ID_1)).thenReturn(Mono.just(exerciseRotationHistory))
        val result = exerciseRotationHistoryController.get(ID_1)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseRotationHistory))
            .verifyComplete()
    }

    @Test
    fun `should return empty when exercise rotation history not found`() {
        whenever(exerciseRotationHistoryService.selectById(NON_EXISTENT_ID)).thenReturn(Mono.empty())
        val result = exerciseRotationHistoryController.get(NON_EXISTENT_ID)
        StepVerifier.create(result)
            .expectComplete()
            .verify()
    }

    @Test
    fun `should create exercise rotation history`() {
        val now = Instant.now()
        val exerciseRotationHistory =
            ExerciseRotationHistory(
                id = ID_1,
                userId = USER_ID,
                exerciseName = BENCH_PRESS,
                isAccessory = false,
                createdAt = now
            )
        whenever(exerciseRotationHistoryService.isOwner(ID_1, USER_ID)).thenReturn(Mono.just(true))
        whenever(exerciseRotationHistoryService.insert(USER_ID, BENCH_PRESS, false)).thenReturn(Mono.just(exerciseRotationHistory))
        val result = exerciseRotationHistoryController.save(USER_ID, BENCH_PRESS, false)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseRotationHistory))
            .verifyComplete()
    }

    @Test
    fun `should update exercise rotation history`() {
        val now = Instant.now()
        val exerciseRotationHistory =
            ExerciseRotationHistory(
                id = ID_1,
                userId = USER_ID,
                exerciseName = BENCH_PRESS,
                isAccessory = true,
                createdAt = now
            )
        whenever(exerciseRotationHistoryService.isOwner(ID_1, USER_ID)).thenReturn(Mono.just(true))
        whenever(exerciseRotationHistoryService.update(ID_1, USER_ID, BENCH_PRESS, true)).thenReturn(Mono.just(exerciseRotationHistory))
        val result = exerciseRotationHistoryController.update(ID_1, USER_ID, BENCH_PRESS, true)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseRotationHistory))
            .verifyComplete()
    }

    @Test
    fun `should return empty when updating non-existent exercise rotation history`() {
        whenever(exerciseRotationHistoryService.isOwner(NON_EXISTENT_ID, USER_ID)).thenReturn(Mono.just(true))
        whenever(exerciseRotationHistoryService.update(NON_EXISTENT_ID, USER_ID, BENCH_PRESS, false))
            .thenReturn(Mono.empty())
        val result = exerciseRotationHistoryController.update(NON_EXISTENT_ID, USER_ID, BENCH_PRESS, false)
        StepVerifier.create(result)
            .expectComplete()
            .verify()
    }

    @Test
    fun `should delete exercise rotation history`() {
        val now = Instant.now()
        val exerciseRotationHistory =
            ExerciseRotationHistory(
                id = ID_1,
                userId = USER_ID,
                exerciseName = BENCH_PRESS,
                isAccessory = false,
                createdAt = now
            )
        whenever(exerciseRotationHistoryService.isOwner(ID_1, USER_ID)).thenReturn(Mono.just(true))
        whenever(exerciseRotationHistoryService.deleteById(ID_1)).thenReturn(Mono.just(exerciseRotationHistory))
        val result = exerciseRotationHistoryController.delete(ID_1)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseRotationHistory))
            .verifyComplete()
    }

    @Test
    fun `should return empty when deleting non-existent exercise rotation history`() {
        whenever(exerciseRotationHistoryService.isOwner(NON_EXISTENT_ID, USER_ID)).thenReturn(Mono.just(true))
        whenever(exerciseRotationHistoryService.deleteById(NON_EXISTENT_ID)).thenReturn(Mono.empty())
        val result = exerciseRotationHistoryController.delete(NON_EXISTENT_ID)
        StepVerifier.create(result)
            .expectComplete()
            .verify()
    }

    @Test
    fun `should get exercise rotation histories by accessory type`() {
        val now = Instant.now()
        val isAccessory = false
        val exerciseRotationHistories =
            listOf(
                ExerciseRotationHistory(
                    id = ID_1,
                    userId = USER_ID,
                    exerciseName = BENCH_PRESS,
                    isAccessory = isAccessory,
                    createdAt = now
                ),
                ExerciseRotationHistory(
                    id = ID_2,
                    userId = USER_ID,
                    exerciseName = SQUAT,
                    isAccessory = isAccessory,
                    createdAt = now
                )
            )
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just("1"))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(setOf("admin")))
        whenever(exerciseRotationHistoryService.selectByIsAccessory(isAccessory)).thenReturn(Mono.just(exerciseRotationHistories))
        val result = exerciseRotationHistoryController.getByIsAccessory(isAccessory)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseRotationHistories))
            .verifyComplete()
    }

    @Test
    fun `should return empty list when no exercise rotation histories for accessory type`() {
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just("1"))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(setOf("admin")))
        whenever(exerciseRotationHistoryService.selectByIsAccessory(true)).thenReturn(Mono.just(emptyList()))
        val result = exerciseRotationHistoryController.getByIsAccessory(true)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(emptyList<ExerciseRotationHistory>()))
            .verifyComplete()
    }

    @Test
    fun `getByIsAccessory returns all items for admin`() {
        val isAccessory = false
        val userId = "1"
        val roles = setOf("admin")
        val histories =
            listOf(
                ExerciseRotationHistory(1L, "1", "Bench Press", isAccessory, Instant.now()),
                ExerciseRotationHistory(2L, "2", "Squat", isAccessory, Instant.now())
            )
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(exerciseRotationHistoryService.selectByIsAccessory(isAccessory)).thenReturn(Mono.just(histories))

        val result = exerciseRotationHistoryController.getByIsAccessory(isAccessory)
        StepVerifier.create(result)
            .assertNext { response ->
                assert(response.body!!.size == 2)
                assert(response.body!!.containsAll(histories))
            }
            .verifyComplete()
    }

    @Test
    fun `getByIsAccessory returns all items for service`() {
        val isAccessory = false
        val userId = "1"
        val roles = setOf("service")
        val histories =
            listOf(
                ExerciseRotationHistory(1L, "1", "Bench Press", isAccessory, Instant.now()),
                ExerciseRotationHistory(2L, "2", "Squat", isAccessory, Instant.now())
            )
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(exerciseRotationHistoryService.selectByIsAccessory(isAccessory)).thenReturn(Mono.just(histories))

        val result = exerciseRotationHistoryController.getByIsAccessory(isAccessory)
        StepVerifier.create(result)
            .assertNext { response ->
                assert(response.body!!.size == 2)
                assert(response.body!!.containsAll(histories))
            }
            .verifyComplete()
    }

    @Test
    fun `getByIsAccessory returns only user items for regular user`() {
        val isAccessory = false
        val userId = "1"
        val roles = setOf("user")
        val histories =
            listOf(
                ExerciseRotationHistory(1L, "1", "Bench Press", isAccessory, Instant.now())
            )
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(exerciseRotationHistoryService.selectByUserId(userId, isAccessory)).thenReturn(Mono.just(histories))

        val result = exerciseRotationHistoryController.getByIsAccessory(isAccessory)
        StepVerifier.create(result)
            .assertNext { response ->
                assert(response.body!!.size == 1)
                assert(response.body!![0].userId == userId)
            }
            .verifyComplete()
    }

    @Test
    fun `getByIsAccessory returns empty for regular user with no items`() {
        val isAccessory = false
        val userId = "3"
        val roles = setOf("user")
        val histories = emptyList<ExerciseRotationHistory>()
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(exerciseRotationHistoryService.selectByUserId(userId, isAccessory)).thenReturn(Mono.just(histories))

        val result = exerciseRotationHistoryController.getByIsAccessory(isAccessory)
        StepVerifier.create(result)
            .assertNext { response ->
                assert(response.body!!.isEmpty())
            }
            .verifyComplete()
    }

    @Test
    fun `getAll returns all items for admin`() {
        val userId = "1"
        val roles = setOf("admin")
        val histories =
            listOf(
                ExerciseRotationHistory(1L, "1", "Bench Press", false, Instant.now()),
                ExerciseRotationHistory(2L, "2", "Squat", false, Instant.now())
            )
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(exerciseRotationHistoryService.selectAll()).thenReturn(Mono.just(histories))

        val result = exerciseRotationHistoryController.getAll()
        StepVerifier.create(result)
            .assertNext { response ->
                assert(response.body!!.size == 2)
                assert(response.body!!.containsAll(histories))
            }
            .verifyComplete()
    }

    @Test
    fun `getAll returns all items for service`() {
        val userId = "1"
        val roles = setOf("service")
        val histories =
            listOf(
                ExerciseRotationHistory(1L, "1", "Bench Press", false, Instant.now()),
                ExerciseRotationHistory(2L, "2", "Squat", false, Instant.now())
            )
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(exerciseRotationHistoryService.selectAll()).thenReturn(Mono.just(histories))

        val result = exerciseRotationHistoryController.getAll()
        StepVerifier.create(result)
            .assertNext { response ->
                assert(response.body!!.size == 2)
                assert(response.body!!.containsAll(histories))
            }
            .verifyComplete()
    }

    @Test
    fun `getAll returns only user items for regular user`() {
        val userId = "1"
        val roles = setOf("user")
        val histories =
            listOf(
                ExerciseRotationHistory(1L, "1", "Bench Press", false, Instant.now())
            )
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(exerciseRotationHistoryService.selectByUserId(userId)).thenReturn(Mono.just(histories))

        val result = exerciseRotationHistoryController.getAll()
        StepVerifier.create(result)
            .assertNext { response ->
                assert(response.body!!.size == 1)
                assert(response.body!![0].userId == userId)
            }
            .verifyComplete()
    }

    @Test
    fun `getAll returns empty for regular user with no items`() {
        val userId = "3"
        val roles = setOf("user")
        val histories = emptyList<ExerciseRotationHistory>()
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(exerciseRotationHistoryService.selectByUserId(userId)).thenReturn(Mono.just(histories))

        val result = exerciseRotationHistoryController.getAll()
        StepVerifier.create(result)
            .assertNext { response ->
                assert(response.body!!.isEmpty())
            }
            .verifyComplete()
    }

    @Test
    fun `should handle DAL error gracefully`() {
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just("1"))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(setOf("admin")))
        whenever(exerciseRotationHistoryService.selectAll()).thenReturn(Mono.error(DatabaseException("Database error")))
        val result = exerciseRotationHistoryController.getAll()
        StepVerifier.create(result)
            .expectError(DatabaseException::class.java)
            .verify()
    }
}

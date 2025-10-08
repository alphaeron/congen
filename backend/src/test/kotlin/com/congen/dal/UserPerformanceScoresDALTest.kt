package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.UserPerformanceScores
import com.congen.service.AuditService
import com.congen.service.LogSensitivity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserPerformanceScoresDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var auditService: AuditService
    private lateinit var userPerformanceScoresDAL: UserPerformanceScoresDAL

    private val testKeycloakId = "test-keycloak-id"
    private val now = Instant.now()
    private val scores =
        UserPerformanceScores(
            keycloakId = testKeycloakId,
            explosivenessScore = 75.0,
            aerobicCapacityScore = 80.0,
            recoveryScore = 70.0,
            reactionTimeScore = 65.0,
            mobilityScore = 85.0,
            strengthScore = 90.0,
            wilksScore = 350.0,
            level = 15,
            levelChangeReason = "test",
            hp = 80.0,
            hpLoss = 10.0,
            mp = 75.0,
            mpLoss = 5.0,
            fatigue = 20.0,
            fatigueLoss = 15.0,
            skills = listOf("Powerhouse", "Iron Lungs"),
            createdAt = now
        )

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        auditService = mock()

        // Mock audit service to return empty Mono
        whenever(auditService.logDataAccess(any(), any(), any(), any())).thenReturn(Mono.empty())

        userPerformanceScoresDAL = UserPerformanceScoresDAL(postgresClient, auditService)
    }

    @Test
    fun `selectUserPerformanceScores should return latest scores`() {
        whenever(
            postgresClient.selectIndividual<UserPerformanceScores>(
                any<String>(),
                any<kotlin.reflect.KClass<UserPerformanceScores>>(),
                any<Array<Any?>>()
            )
        )
            .thenReturn(Mono.just(scores))

        val result = userPerformanceScoresDAL.selectUserPerformanceScores(testKeycloakId)

        StepVerifier.create(result)
            .assertNext { result ->
                assert(result.keycloakId == scores.keycloakId)
                assert(result.explosivenessScore == scores.explosivenessScore)
                assert(result.aerobicCapacityScore == scores.aerobicCapacityScore)
                assert(result.recoveryScore == scores.recoveryScore)
                assert(result.reactionTimeScore == scores.reactionTimeScore)
                assert(result.mobilityScore == scores.mobilityScore)
                assert(result.strengthScore == scores.strengthScore)
                assert(result.wilksScore == scores.wilksScore)
                assert(result.level == scores.level)
                assert(result.levelChangeReason == scores.levelChangeReason)
                assert(result.hp == scores.hp)
                assert(result.hpLoss == scores.hpLoss)
                assert(result.mp == scores.mp)
                assert(result.mpLoss == scores.mpLoss)
                assert(result.fatigue == scores.fatigue)
                assert(result.fatigueLoss == scores.fatigueLoss)
                assert(result.skills == scores.skills)
            }
            .verifyComplete()

        verify(auditService).logDataAccess("user_performance_scores", "SELECT_LATEST", testKeycloakId, LogSensitivity.LOW)
        verify(
            postgresClient
        ).selectIndividual<UserPerformanceScores>(any<String>(), any<kotlin.reflect.KClass<UserPerformanceScores>>(), eq(testKeycloakId))
    }

    @Test
    fun `selectUserPerformanceScoresInRange should return scores in range`() {
        val startTimestamp = now.minusSeconds(86400)
        val endTimestamp = now
        val scoresList = listOf(scores)

        whenever(
            postgresClient.select<UserPerformanceScores>(
                any<String>(),
                any<kotlin.reflect.KClass<UserPerformanceScores>>(),
                any<Array<Any?>>()
            )
        )
            .thenReturn(Mono.just(scoresList))

        val result = userPerformanceScoresDAL.selectUserPerformanceScoresInRange(testKeycloakId, startTimestamp, endTimestamp)

        StepVerifier.create(result)
            .expectNext(scoresList)
            .verifyComplete()

        verify(auditService).logDataAccess("user_performance_scores", "SELECT_RANGE", testKeycloakId, LogSensitivity.LOW)
        verify(
            postgresClient
        ).select<UserPerformanceScores>(
            any<String>(),
            any<kotlin.reflect.KClass<UserPerformanceScores>>(),
            eq(testKeycloakId),
            anyOrNull(),
            anyOrNull()
        )
    }

    @Test
    fun `selectUserPerformanceScoresInRange should return all scores when no range specified`() {
        val scoresList = listOf(scores)

        whenever(
            postgresClient.select<UserPerformanceScores>(
                any<String>(),
                any<kotlin.reflect.KClass<UserPerformanceScores>>(),
                any<Array<Any?>>()
            )
        )
            .thenReturn(Mono.just(scoresList))

        val result = userPerformanceScoresDAL.selectUserPerformanceScoresInRange(testKeycloakId, null, null)

        StepVerifier.create(result)
            .expectNext(scoresList)
            .verifyComplete()

        verify(auditService).logDataAccess("user_performance_scores", "SELECT_ALL", testKeycloakId, LogSensitivity.LOW)
        verify(
            postgresClient
        ).select<UserPerformanceScores>(any<String>(), any<kotlin.reflect.KClass<UserPerformanceScores>>(), eq(testKeycloakId))
    }

    @Test
    fun `insertUserPerformanceScores should insert new scores`() {
        whenever(
            postgresClient.update<UserPerformanceScores>(
                any<String>(),
                any<kotlin.reflect.KClass<UserPerformanceScores>>(),
                any<Array<Any?>>()
            )
        )
            .thenReturn(Mono.just(scores))

        val result = userPerformanceScoresDAL.insertUserPerformanceScores(scores)

        StepVerifier.create(result)
            .assertNext { result ->
                assert(result.keycloakId == scores.keycloakId)
                assert(result.explosivenessScore == scores.explosivenessScore)
                assert(result.aerobicCapacityScore == scores.aerobicCapacityScore)
                assert(result.recoveryScore == scores.recoveryScore)
                assert(result.reactionTimeScore == scores.reactionTimeScore)
                assert(result.mobilityScore == scores.mobilityScore)
                assert(result.strengthScore == scores.strengthScore)
                assert(result.wilksScore == scores.wilksScore)
                assert(result.level == scores.level)
                assert(result.levelChangeReason == scores.levelChangeReason)
                assert(result.hp == scores.hp)
                assert(result.hpLoss == scores.hpLoss)
                assert(result.mp == scores.mp)
                assert(result.mpLoss == scores.mpLoss)
                assert(result.fatigue == scores.fatigue)
                assert(result.fatigueLoss == scores.fatigueLoss)
                assert(result.skills == scores.skills)
            }
            .verifyComplete()

        verify(auditService).logDataAccess("user_performance_scores", "INSERT", testKeycloakId, LogSensitivity.LOW)
        verify(
            postgresClient
        ).update<UserPerformanceScores>(any<String>(), any<kotlin.reflect.KClass<UserPerformanceScores>>(), any<Array<Any?>>())
    }

    @Test
    fun `upsertUserPerformanceScores should update existing scores for same day`() {
        val existingScores = scores.copy(level = 10)
        val updatedScores = scores.copy(level = 15)

        whenever(
            postgresClient.selectIndividual<UserPerformanceScores>(
                any<String>(),
                any<kotlin.reflect.KClass<UserPerformanceScores>>(),
                any<Array<Any?>>()
            )
        )
            .thenReturn(Mono.just(existingScores))
        whenever(
            postgresClient.update<UserPerformanceScores>(
                any<String>(),
                any<kotlin.reflect.KClass<UserPerformanceScores>>(),
                any<Array<Any?>>()
            )
        )
            .thenReturn(Mono.just(updatedScores))

        val result = userPerformanceScoresDAL.upsertUserPerformanceScores(scores)

        StepVerifier.create(result)
            .assertNext { result ->
                assert(result.keycloakId == updatedScores.keycloakId)
                assert(result.explosivenessScore == updatedScores.explosivenessScore)
                assert(result.aerobicCapacityScore == updatedScores.aerobicCapacityScore)
                assert(result.recoveryScore == updatedScores.recoveryScore)
                assert(result.reactionTimeScore == updatedScores.reactionTimeScore)
                assert(result.mobilityScore == updatedScores.mobilityScore)
                assert(result.strengthScore == updatedScores.strengthScore)
                assert(result.wilksScore == updatedScores.wilksScore)
                assert(result.level == updatedScores.level)
                assert(result.levelChangeReason == updatedScores.levelChangeReason)
                assert(result.hp == updatedScores.hp)
                assert(result.hpLoss == updatedScores.hpLoss)
                assert(result.mp == updatedScores.mp)
                assert(result.mpLoss == updatedScores.mpLoss)
                assert(result.fatigue == updatedScores.fatigue)
                assert(result.fatigueLoss == updatedScores.fatigueLoss)
                assert(result.skills == updatedScores.skills)
            }
            .verifyComplete()

        verify(auditService).logDataAccess("user_performance_scores", "UPSERT", testKeycloakId, LogSensitivity.LOW)
        verify(
            postgresClient
        ).update<UserPerformanceScores>(any<String>(), any<kotlin.reflect.KClass<UserPerformanceScores>>(), any<Array<Any?>>())
    }

    @Test
    fun `upsertUserPerformanceScores should insert new scores for different day`() {
        val existingScores = scores.copy(createdAt = now.minusSeconds(86400)) // Previous day
        val newScores = scores.copy(createdAt = now)

        whenever(
            postgresClient.selectIndividual<UserPerformanceScores>(
                any<String>(),
                any<kotlin.reflect.KClass<UserPerformanceScores>>(),
                any<Array<Any?>>()
            )
        )
            .thenReturn(Mono.just(existingScores))
        whenever(
            postgresClient.update<UserPerformanceScores>(
                any<String>(),
                any<kotlin.reflect.KClass<UserPerformanceScores>>(),
                any<Array<Any?>>()
            )
        )
            .thenReturn(Mono.just(newScores))

        val result = userPerformanceScoresDAL.upsertUserPerformanceScores(scores)

        StepVerifier.create(result)
            .assertNext { result ->
                assert(result.keycloakId == newScores.keycloakId)
                assert(result.explosivenessScore == newScores.explosivenessScore)
                assert(result.aerobicCapacityScore == newScores.aerobicCapacityScore)
                assert(result.recoveryScore == newScores.recoveryScore)
                assert(result.reactionTimeScore == newScores.reactionTimeScore)
                assert(result.mobilityScore == newScores.mobilityScore)
                assert(result.strengthScore == newScores.strengthScore)
                assert(result.wilksScore == newScores.wilksScore)
                assert(result.level == newScores.level)
                assert(result.levelChangeReason == newScores.levelChangeReason)
                assert(result.hp == newScores.hp)
                assert(result.hpLoss == newScores.hpLoss)
                assert(result.mp == newScores.mp)
                assert(result.mpLoss == newScores.mpLoss)
                assert(result.fatigue == newScores.fatigue)
                assert(result.fatigueLoss == newScores.fatigueLoss)
                assert(result.skills == newScores.skills)
            }
            .verifyComplete()

        verify(auditService).logDataAccess("user_performance_scores", "UPSERT", testKeycloakId, LogSensitivity.LOW)
        verify(
            postgresClient
        ).update<UserPerformanceScores>(any<String>(), any<kotlin.reflect.KClass<UserPerformanceScores>>(), any<Array<Any?>>())
    }

    @Test
    fun `upsertUserPerformanceScores should insert new scores when no existing scores`() {
        whenever(
            postgresClient.selectIndividual<UserPerformanceScores>(
                any<String>(),
                any<kotlin.reflect.KClass<UserPerformanceScores>>(),
                any<Array<Any?>>()
            )
        )
            .thenReturn(Mono.error(NoResultsFoundException("No scores found")))
        whenever(
            postgresClient.update<UserPerformanceScores>(
                any<String>(),
                any<kotlin.reflect.KClass<UserPerformanceScores>>(),
                any<Array<Any?>>()
            )
        )
            .thenReturn(Mono.just(scores))

        val result = userPerformanceScoresDAL.upsertUserPerformanceScores(scores)

        StepVerifier.create(result)
            .assertNext { result ->
                assert(result.keycloakId == scores.keycloakId)
                assert(result.explosivenessScore == scores.explosivenessScore)
                assert(result.aerobicCapacityScore == scores.aerobicCapacityScore)
                assert(result.recoveryScore == scores.recoveryScore)
                assert(result.reactionTimeScore == scores.reactionTimeScore)
                assert(result.mobilityScore == scores.mobilityScore)
                assert(result.strengthScore == scores.strengthScore)
                assert(result.wilksScore == scores.wilksScore)
                assert(result.level == scores.level)
                assert(result.levelChangeReason == scores.levelChangeReason)
                assert(result.hp == scores.hp)
                assert(result.hpLoss == scores.hpLoss)
                assert(result.mp == scores.mp)
                assert(result.mpLoss == scores.mpLoss)
                assert(result.fatigue == scores.fatigue)
                assert(result.fatigueLoss == scores.fatigueLoss)
                assert(result.skills == scores.skills)
            }
            .verifyComplete()

        verify(auditService).logDataAccess("user_performance_scores", "UPSERT", testKeycloakId, LogSensitivity.LOW)
        verify(
            postgresClient
        ).update<UserPerformanceScores>(any<String>(), any<kotlin.reflect.KClass<UserPerformanceScores>>(), any<Array<Any?>>())
    }

    @Test
    fun `upsertUserPerformanceScores should propagate non-NoResultsFoundException errors`() {
        val error = RuntimeException("Database error")
        whenever(
            postgresClient.selectIndividual<UserPerformanceScores>(
                any<String>(),
                any<kotlin.reflect.KClass<UserPerformanceScores>>(),
                any<Array<Any?>>()
            )
        )
            .thenReturn(Mono.error(error))

        val result = userPerformanceScoresDAL.upsertUserPerformanceScores(scores)

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()

        verify(auditService).logDataAccess("user_performance_scores", "UPSERT", testKeycloakId, LogSensitivity.LOW)
    }
}

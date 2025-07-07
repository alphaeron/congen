package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.exceptions.ValidationException
import com.congen.model.SetScheme
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.LocalDateTime

class SetSchemeDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var setSchemeDAL: SetSchemeDAL
    private val now = LocalDateTime.now()

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        setSchemeDAL = SetSchemeDAL(postgresClient)
    }

    @Test
    fun `selectSetSchemeById should return set scheme`() {
        val setScheme =
            SetScheme(
                id = 1L,
                programmedExerciseId = 5L,
                setNumber = 1,
                isAmrap = false,
                isEmom = false,
                useTempo = true,
                eccentricTempo = "3",
                isometricTempo = "1",
                concentricTempo = "1",
                targetWeight = BigDecimal("100.0"),
                performedWeight = BigDecimal("100.0"),
                targetRepCount = 5,
                performedRepCount = 5,
                restSeconds = 180,
                createdAt = now,
                updatedAt = now
            )

        whenever(
            postgresClient.selectIndividual<SetScheme>(
                "SELECT * FROM set_scheme WHERE id=$1",
                1L
            )
        ).thenReturn(Mono.just(setScheme))

        val result = setSchemeDAL.selectSetSchemeById(1L)

        StepVerifier.create(result)
            .expectNext(setScheme)
            .verifyComplete()

        verify(postgresClient).selectIndividual<SetScheme>(
            "SELECT * FROM set_scheme WHERE id=$1",
            1L
        )
    }

    @Test
    fun `selectSetSchemesByProgrammedExerciseId should return list of set schemes`() {
        val setSchemes =
            listOf(
                SetScheme(
                    id = 1L,
                    programmedExerciseId = 5L,
                    setNumber = 1,
                    isAmrap = false,
                    isEmom = false,
                    useTempo = false,
                    eccentricTempo = null,
                    isometricTempo = null,
                    concentricTempo = null,
                    targetWeight = BigDecimal("100.0"),
                    performedWeight = BigDecimal("100.0"),
                    targetRepCount = 5,
                    performedRepCount = 5,
                    restSeconds = 180,
                    createdAt = now,
                    updatedAt = now
                ),
                SetScheme(
                    id = 2L,
                    programmedExerciseId = 5L,
                    setNumber = 2,
                    isAmrap = false,
                    isEmom = false,
                    useTempo = false,
                    eccentricTempo = null,
                    isometricTempo = null,
                    concentricTempo = null,
                    targetWeight = BigDecimal("100.0"),
                    performedWeight = BigDecimal("100.0"),
                    targetRepCount = 5,
                    performedRepCount = 5,
                    restSeconds = 180,
                    createdAt = now,
                    updatedAt = now
                )
            )

        whenever(
            postgresClient.select<SetScheme>(
                "SELECT * FROM set_scheme WHERE programmed_exercise_id=$1 ORDER BY set_number",
                5L
            )
        ).thenReturn(Mono.just(setSchemes))

        val result = setSchemeDAL.selectSetSchemesByProgrammedExerciseId(5L)

        StepVerifier.create(result)
            .expectNext(setSchemes)
            .verifyComplete()

        verify(postgresClient).select<SetScheme>(
            "SELECT * FROM set_scheme WHERE programmed_exercise_id=$1 ORDER BY set_number",
            5L
        )
    }

    @Test
    fun `selectSetSchemes should return all set schemes`() {
        val setSchemes =
            listOf(
                SetScheme(
                    id = 1L,
                    programmedExerciseId = 5L,
                    setNumber = 1,
                    isAmrap = false,
                    isEmom = false,
                    useTempo = false,
                    eccentricTempo = null,
                    isometricTempo = null,
                    concentricTempo = null,
                    targetWeight = BigDecimal("100.0"),
                    performedWeight = BigDecimal("100.0"),
                    targetRepCount = 5,
                    performedRepCount = 5,
                    restSeconds = 180,
                    createdAt = now,
                    updatedAt = now
                )
            )

        whenever(
            postgresClient.select<SetScheme>("SELECT * FROM set_scheme ORDER BY programmed_exercise_id, set_number")
        ).thenReturn(Mono.just(setSchemes))

        val result = setSchemeDAL.selectSetSchemes()

        StepVerifier.create(result)
            .expectNext(setSchemes)
            .verifyComplete()

        verify(postgresClient).select<SetScheme>("SELECT * FROM set_scheme ORDER BY programmed_exercise_id, set_number")
    }

    @Test
    fun `insertSetScheme should return created set scheme`() {
        val createdSetScheme =
            SetScheme(
                id = 1L,
                programmedExerciseId = 5L,
                setNumber = 1,
                isAmrap = false,
                isEmom = false,
                useTempo = true,
                eccentricTempo = "3",
                isometricTempo = "1",
                concentricTempo = "1",
                targetWeight = BigDecimal("100.0"),
                performedWeight = BigDecimal("100.0"),
                targetRepCount = 5,
                performedRepCount = 5,
                restSeconds = 180,
                createdAt = now,
                updatedAt = now
            )
        val expectedQuery =
            """
            INSERT INTO set_scheme
                (programmed_exercise_id, set_number, is_amrap, is_emom, use_tempo,
                 eccentric_tempo, isometric_tempo, concentric_tempo, target_weight, performed_weight,
                 target_rep_count, performed_rep_count, rest_seconds)
            VALUES
                ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13)
            """.trimIndent()

        whenever(
            postgresClient.update<SetScheme>(
                expectedQuery,
                createdSetScheme.programmedExerciseId,
                createdSetScheme.setNumber,
                createdSetScheme.isAmrap,
                createdSetScheme.isEmom,
                createdSetScheme.useTempo,
                createdSetScheme.eccentricTempo,
                createdSetScheme.isometricTempo,
                createdSetScheme.concentricTempo,
                createdSetScheme.targetWeight,
                createdSetScheme.performedWeight,
                createdSetScheme.targetRepCount,
                createdSetScheme.performedRepCount,
                createdSetScheme.restSeconds,
            ),
        ).thenReturn(Mono.just(createdSetScheme))
        val result =
            setSchemeDAL.insertSetScheme(
                createdSetScheme.programmedExerciseId,
                createdSetScheme.setNumber,
                createdSetScheme.isAmrap,
                createdSetScheme.isEmom,
                createdSetScheme.useTempo,
                createdSetScheme.eccentricTempo,
                createdSetScheme.isometricTempo,
                createdSetScheme.concentricTempo,
                createdSetScheme.targetWeight,
                createdSetScheme.performedWeight,
                createdSetScheme.targetRepCount,
                createdSetScheme.performedRepCount,
                createdSetScheme.restSeconds
            )
        StepVerifier.create(result).expectNext(createdSetScheme).verifyComplete()
        verify(postgresClient).update<SetScheme>(
            expectedQuery,
            createdSetScheme.programmedExerciseId,
            createdSetScheme.setNumber,
            createdSetScheme.isAmrap,
            createdSetScheme.isEmom,
            createdSetScheme.useTempo,
            createdSetScheme.eccentricTempo,
            createdSetScheme.isometricTempo,
            createdSetScheme.concentricTempo,
            createdSetScheme.targetWeight,
            createdSetScheme.performedWeight,
            createdSetScheme.targetRepCount,
            createdSetScheme.performedRepCount,
            createdSetScheme.restSeconds,
        )
    }

    @Test
    fun `insertSetScheme should throw ValidationException for invalid set number`() {
        assertThrows<ValidationException> {
            setSchemeDAL.insertSetScheme(
                programmedExerciseId = 5L,
                setNumber = 0,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = BigDecimal("100.0"),
                performedWeight = BigDecimal("100.0"),
                targetRepCount = 5,
                performedRepCount = 5,
                restSeconds = 180
            )
        }
    }

    @Test
    fun `insertSetScheme should throw ValidationException for invalid tempo`() {
        assertThrows<ValidationException> {
            setSchemeDAL.insertSetScheme(
                programmedExerciseId = 5L,
                setNumber = 1,
                isAmrap = false,
                isEmom = false,
                useTempo = true,
                eccentricTempo = "10",
                isometricTempo = "1",
                concentricTempo = "1",
                targetWeight = BigDecimal("100.0"),
                performedWeight = BigDecimal("100.0"),
                targetRepCount = 5,
                performedRepCount = 5,
                restSeconds = 180
            )
        }
    }

    @Test
    fun `insertSetScheme should throw ValidationException for invalid target weight`() {
        assertThrows<ValidationException> {
            setSchemeDAL.insertSetScheme(
                programmedExerciseId = 5L,
                setNumber = 1,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = BigDecimal.ZERO,
                performedWeight = BigDecimal("100.0"),
                targetRepCount = 5,
                performedRepCount = 5,
                restSeconds = 180
            )
        }
    }

    @Test
    fun `updateSetScheme should return updated set scheme`() {
        val setScheme =
            SetScheme(
                id = 1L,
                programmedExerciseId = 5L,
                setNumber = 1,
                isAmrap = false,
                isEmom = false,
                useTempo = true,
                eccentricTempo = "3",
                isometricTempo = "1",
                concentricTempo = "1",
                targetWeight = BigDecimal("100.0"),
                performedWeight = BigDecimal("100.0"),
                targetRepCount = 5,
                performedRepCount = 5,
                restSeconds = 180,
                createdAt = now,
                updatedAt = now
            )
        val expectedQuery =
            """
            UPDATE set_scheme
            SET programmed_exercise_id=$2, set_number=$3, is_amrap=$4, is_emom=$5, use_tempo=$6,
                eccentric_tempo=$7, isometric_tempo=$8, concentric_tempo=$9, target_weight=$10, performed_weight=$11,
                target_rep_count=$12, performed_rep_count=$13, rest_seconds=$14, updated_at=NOW()
            WHERE id=$1
            """.trimIndent()

        whenever(
            postgresClient.update<SetScheme>(
                expectedQuery,
                setScheme.id,
                setScheme.programmedExerciseId,
                setScheme.setNumber,
                setScheme.isAmrap,
                setScheme.isEmom,
                setScheme.useTempo,
                setScheme.eccentricTempo,
                setScheme.isometricTempo,
                setScheme.concentricTempo,
                setScheme.targetWeight,
                setScheme.performedWeight,
                setScheme.targetRepCount,
                setScheme.performedRepCount,
                setScheme.restSeconds,
            ),
        ).thenReturn(Mono.just(setScheme))
        val result =
            setSchemeDAL.updateSetScheme(
                setScheme.id,
                setScheme.programmedExerciseId,
                setScheme.setNumber,
                setScheme.isAmrap,
                setScheme.isEmom,
                setScheme.useTempo,
                setScheme.eccentricTempo,
                setScheme.isometricTempo,
                setScheme.concentricTempo,
                setScheme.targetWeight,
                setScheme.performedWeight,
                setScheme.targetRepCount,
                setScheme.performedRepCount,
                setScheme.restSeconds
            )
        StepVerifier.create(result).expectNext(setScheme).verifyComplete()
        verify(postgresClient).update<SetScheme>(
            expectedQuery,
            setScheme.id,
            setScheme.programmedExerciseId,
            setScheme.setNumber,
            setScheme.isAmrap,
            setScheme.isEmom,
            setScheme.useTempo,
            setScheme.eccentricTempo,
            setScheme.isometricTempo,
            setScheme.concentricTempo,
            setScheme.targetWeight,
            setScheme.performedWeight,
            setScheme.targetRepCount,
            setScheme.performedRepCount,
            setScheme.restSeconds,
        )
    }

    @Test
    fun `updateSetScheme should throw ValidationException for invalid set number`() {
        assertThrows<ValidationException> {
            setSchemeDAL.updateSetScheme(
                id = 1L,
                programmedExerciseId = 5L,
                setNumber = 0,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = BigDecimal("100.0"),
                performedWeight = BigDecimal("100.0"),
                targetRepCount = 5,
                performedRepCount = 5,
                restSeconds = 180
            )
        }
    }

    @Test
    fun `deleteSetScheme should return deleted set scheme`() {
        val deletedSetScheme =
            SetScheme(
                id = 1L,
                programmedExerciseId = 5L,
                setNumber = 1,
                isAmrap = false,
                isEmom = false,
                useTempo = false,
                eccentricTempo = null,
                isometricTempo = null,
                concentricTempo = null,
                targetWeight = BigDecimal("100.0"),
                performedWeight = BigDecimal("100.0"),
                targetRepCount = 5,
                performedRepCount = 5,
                restSeconds = 180,
                createdAt = now,
                updatedAt = now
            )

        whenever(
            postgresClient.update<SetScheme>(
                """
                DELETE FROM set_scheme WHERE id=$1
                """.trimIndent(),
                1L
            )
        ).thenReturn(Mono.just(deletedSetScheme))

        val result = setSchemeDAL.deleteSetScheme(1L)

        StepVerifier.create(result)
            .expectNext(deletedSetScheme)
            .verifyComplete()

        verify(postgresClient).update<SetScheme>(
            """
            DELETE FROM set_scheme WHERE id=$1
            """.trimIndent(),
            1L
        )
    }
}

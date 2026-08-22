package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.exceptions.ValidationException
import com.congen.mockSetScheme
import com.congen.model.Band
import com.congen.model.SetScheme
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal

class SetSchemeDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var setSchemeDAL: SetSchemeDAL

    private val setScheme = mockSetScheme()
    private val setSchemeList = listOf(setScheme, mockSetScheme(id = 2L, setNumber = 2))
    private val allSetSchemes = listOf(setScheme)

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        setSchemeDAL = SetSchemeDAL(postgresClient)
    }

    @Test
    fun `selectSetSchemeById should return set scheme`() {
        whenever(
            postgresClient.selectIndividual<SetScheme>(
                "SELECT * FROM set_scheme WHERE id=$1",
                setScheme.id
            )
        ).thenReturn(Mono.just(setScheme))

        val result = setSchemeDAL.selectSetSchemeById(setScheme.id)

        StepVerifier.create(result)
            .expectNext(setScheme)
            .verifyComplete()

        verify(postgresClient).selectIndividual<SetScheme>(
            "SELECT * FROM set_scheme WHERE id=$1",
            setScheme.id
        )
    }

    @Test
    fun `selectSetSchemesByProgrammedExerciseId should return list of set schemes`() {
        whenever(
            postgresClient.select<SetScheme>(
                "SELECT * FROM set_scheme WHERE programmed_exercise_id=$1 ORDER BY set_number",
                setScheme.programmedExerciseId
            )
        ).thenReturn(Mono.just(setSchemeList))

        val result = setSchemeDAL.selectSetSchemesByProgrammedExerciseId(setScheme.programmedExerciseId)

        StepVerifier.create(result)
            .expectNext(setSchemeList)
            .verifyComplete()

        verify(postgresClient).select<SetScheme>(
            "SELECT * FROM set_scheme WHERE programmed_exercise_id=$1 ORDER BY set_number",
            setScheme.programmedExerciseId
        )
    }

    @Test
    fun `selectSetSchemes should return all set schemes`() {
        whenever(
            postgresClient.select<SetScheme>("SELECT * FROM set_scheme ORDER BY programmed_exercise_id, set_number")
        ).thenReturn(Mono.just(allSetSchemes))

        val result = setSchemeDAL.selectSetSchemes()

        StepVerifier.create(result)
            .expectNext(allSetSchemes)
            .verifyComplete()

        verify(postgresClient).select<SetScheme>("SELECT * FROM set_scheme ORDER BY programmed_exercise_id, set_number")
    }

    @Test
    fun `insertSetScheme should return created set scheme`() {
        val createdSetScheme = mockSetScheme()
        val expectedQuery =
            """
            INSERT INTO set_scheme
                (programmed_exercise_id, set_number, is_amrap, is_emom, use_tempo,
                 eccentric_tempo, isometric_tempo, concentric_tempo, target_weight, performed_weight,
                 target_rep_count, performed_rep_count, rest_seconds, band_weight_lbs)
            VALUES
                ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14)
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
                null,
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
                createdSetScheme.restSeconds,
                band = null
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
            null,
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
                targetWeight = BigDecimal("-1.0"),
                performedWeight = BigDecimal("100.0"),
                targetRepCount = 5,
                performedRepCount = 5,
                restSeconds = 180
            )
        }
    }

    @Test
    fun `updateSetScheme should return updated set scheme without changing band weight when not requested`() {
        val updatedSetScheme = mockSetScheme()

        whenever(
            postgresClient.updateLiteral(
                any(),
                eq(SetScheme::class),
                eq(updatedSetScheme.id),
                eq(updatedSetScheme.programmedExerciseId),
                eq(updatedSetScheme.setNumber),
                eq(updatedSetScheme.isAmrap),
                eq(updatedSetScheme.isEmom),
                eq(updatedSetScheme.useTempo),
                eq(updatedSetScheme.eccentricTempo),
                eq(updatedSetScheme.isometricTempo),
                eq(updatedSetScheme.concentricTempo),
                eq(updatedSetScheme.targetWeight),
                eq(updatedSetScheme.performedWeight),
                eq(updatedSetScheme.targetRepCount),
                eq(updatedSetScheme.performedRepCount),
                eq(updatedSetScheme.restSeconds),
            ),
        ).thenReturn(Mono.just(updatedSetScheme))
        val result =
            setSchemeDAL.updateSetScheme(
                updatedSetScheme.id,
                updatedSetScheme.programmedExerciseId,
                updatedSetScheme.setNumber,
                updatedSetScheme.isAmrap,
                updatedSetScheme.isEmom,
                updatedSetScheme.useTempo,
                updatedSetScheme.eccentricTempo,
                updatedSetScheme.isometricTempo,
                updatedSetScheme.concentricTempo,
                updatedSetScheme.targetWeight,
                updatedSetScheme.performedWeight,
                updatedSetScheme.targetRepCount,
                updatedSetScheme.performedRepCount,
                updatedSetScheme.restSeconds,
            )
        StepVerifier.create(result).expectNext(updatedSetScheme).verifyComplete()
        verify(postgresClient).updateLiteral(
            any(),
            eq(SetScheme::class),
            eq(updatedSetScheme.id),
            eq(updatedSetScheme.programmedExerciseId),
            eq(updatedSetScheme.setNumber),
            eq(updatedSetScheme.isAmrap),
            eq(updatedSetScheme.isEmom),
            eq(updatedSetScheme.useTempo),
            eq(updatedSetScheme.eccentricTempo),
            eq(updatedSetScheme.isometricTempo),
            eq(updatedSetScheme.concentricTempo),
            eq(updatedSetScheme.targetWeight),
            eq(updatedSetScheme.performedWeight),
            eq(updatedSetScheme.targetRepCount),
            eq(updatedSetScheme.performedRepCount),
            eq(updatedSetScheme.restSeconds),
        )
    }

    @Test
    fun `updateSetScheme should update band weight when requested`() {
        val updatedSetScheme = mockSetScheme()
        val band = Band(BigDecimal("60"))

        whenever(
            postgresClient.updateLiteral(
                any(),
                eq(SetScheme::class),
                eq(updatedSetScheme.id),
                eq(updatedSetScheme.programmedExerciseId),
                eq(updatedSetScheme.setNumber),
                eq(updatedSetScheme.isAmrap),
                eq(updatedSetScheme.isEmom),
                eq(updatedSetScheme.useTempo),
                eq(updatedSetScheme.eccentricTempo),
                eq(updatedSetScheme.isometricTempo),
                eq(updatedSetScheme.concentricTempo),
                eq(updatedSetScheme.targetWeight),
                eq(updatedSetScheme.performedWeight),
                eq(updatedSetScheme.targetRepCount),
                eq(updatedSetScheme.performedRepCount),
                eq(updatedSetScheme.restSeconds),
                eq(band.weightLbs.toDouble()),
            ),
        ).thenReturn(Mono.just(updatedSetScheme))
        val result =
            setSchemeDAL.updateSetScheme(
                updatedSetScheme.id,
                updatedSetScheme.programmedExerciseId,
                updatedSetScheme.setNumber,
                updatedSetScheme.isAmrap,
                updatedSetScheme.isEmom,
                updatedSetScheme.useTempo,
                updatedSetScheme.eccentricTempo,
                updatedSetScheme.isometricTempo,
                updatedSetScheme.concentricTempo,
                updatedSetScheme.targetWeight,
                updatedSetScheme.performedWeight,
                updatedSetScheme.targetRepCount,
                updatedSetScheme.performedRepCount,
                updatedSetScheme.restSeconds,
                band = band,
            )
        StepVerifier.create(result).expectNext(updatedSetScheme).verifyComplete()
        verify(postgresClient).updateLiteral(
            any(),
            eq(SetScheme::class),
            eq(updatedSetScheme.id),
            eq(updatedSetScheme.programmedExerciseId),
            eq(updatedSetScheme.setNumber),
            eq(updatedSetScheme.isAmrap),
            eq(updatedSetScheme.isEmom),
            eq(updatedSetScheme.useTempo),
            eq(updatedSetScheme.eccentricTempo),
            eq(updatedSetScheme.isometricTempo),
            eq(updatedSetScheme.concentricTempo),
            eq(updatedSetScheme.targetWeight),
            eq(updatedSetScheme.performedWeight),
            eq(updatedSetScheme.targetRepCount),
            eq(updatedSetScheme.performedRepCount),
            eq(updatedSetScheme.restSeconds),
            eq(band.weightLbs.toDouble()),
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
                restSeconds = 180,
                band = null
            )
        }
    }

    @Test
    fun `deleteSetScheme should return deleted set scheme`() {
        val deletedSetScheme = mockSetScheme()
        val expectedQuery =
            """
            DELETE FROM set_scheme WHERE id=$1
            """.trimIndent()

        whenever(
            postgresClient.update<SetScheme>(
                expectedQuery,
                deletedSetScheme.id
            )
        ).thenReturn(Mono.just(deletedSetScheme))

        val result = setSchemeDAL.deleteSetScheme(deletedSetScheme.id)

        StepVerifier.create(result)
            .expectNext(deletedSetScheme)
            .verifyComplete()

        verify(postgresClient).update<SetScheme>(
            expectedQuery,
            deletedSetScheme.id
        )
    }

    @Test
    fun `selectSetSchemesByUserId should return list of user owned set schemes`() {
        val userId = "b226d772-c063-4974-ae08-ab64134abbcf"
        val userSetSchemes =
            listOf(
                mockSetScheme(id = 1L, programmedExerciseId = 1L, setNumber = 1),
                mockSetScheme(id = 2L, programmedExerciseId = 1L, setNumber = 2)
            )

        whenever(
            postgresClient.select<SetScheme>(
                """
                SELECT ss.*
                FROM set_scheme ss
                JOIN programmed_exercise pe ON ss.programmed_exercise_id = pe.id
                JOIN workout_stage ws ON pe.workout_stage_id = ws.id
                JOIN programmed_workout pw ON ws.programmed_workout_id = pw.id
                JOIN program p ON pw.program_id = p.id
                WHERE p.user_id = $1
                ORDER BY ss.programmed_exercise_id, ss.set_number
                """.trimIndent(),
                userId
            )
        ).thenReturn(Mono.just(userSetSchemes))

        val result = setSchemeDAL.selectSetSchemesByUserId(userId)

        StepVerifier.create(result).expectNext(userSetSchemes).verifyComplete()
        verify(postgresClient).select<SetScheme>(
            """
            SELECT ss.*
            FROM set_scheme ss
            JOIN programmed_exercise pe ON ss.programmed_exercise_id = pe.id
            JOIN workout_stage ws ON pe.workout_stage_id = ws.id
            JOIN programmed_workout pw ON ws.programmed_workout_id = pw.id
            JOIN program p ON pw.program_id = p.id
            WHERE p.user_id = $1
            ORDER BY ss.programmed_exercise_id, ss.set_number
            """.trimIndent(),
            userId
        )
    }

    @Test
    fun `selectSetSchemesByUserId should return empty list when user has no set schemes`() {
        val userId = "b226d772-c063-4974-ae08-ab64134abbcf"
        val emptyList = emptyList<SetScheme>()

        whenever(
            postgresClient.select<SetScheme>(
                """
                SELECT ss.*
                FROM set_scheme ss
                JOIN programmed_exercise pe ON ss.programmed_exercise_id = pe.id
                JOIN workout_stage ws ON pe.workout_stage_id = ws.id
                JOIN programmed_workout pw ON ws.programmed_workout_id = pw.id
                JOIN program p ON pw.program_id = p.id
                WHERE p.user_id = $1
                ORDER BY ss.programmed_exercise_id, ss.set_number
                """.trimIndent(),
                userId
            )
        ).thenReturn(Mono.just(emptyList))

        val result = setSchemeDAL.selectSetSchemesByUserId(userId)

        StepVerifier.create(result).expectNext(emptyList).verifyComplete()
        verify(postgresClient).select<SetScheme>(
            """
            SELECT ss.*
            FROM set_scheme ss
            JOIN programmed_exercise pe ON ss.programmed_exercise_id = pe.id
            JOIN workout_stage ws ON pe.workout_stage_id = ws.id
            JOIN programmed_workout pw ON ws.programmed_workout_id = pw.id
            JOIN program p ON pw.program_id = p.id
            WHERE p.user_id = $1
            ORDER BY ss.programmed_exercise_id, ss.set_number
            """.trimIndent(),
            userId
        )
    }

    @Test
    fun `selectSetSchemesByUserId should propagate database errors`() {
        val userId = "b226d772-c063-4974-ae08-ab64134abbcf"
        val databaseError = RuntimeException("Database connection failed")

        whenever(
            postgresClient.select<SetScheme>(
                """
                SELECT ss.*
                FROM set_scheme ss
                JOIN programmed_exercise pe ON ss.programmed_exercise_id = pe.id
                JOIN workout_stage ws ON pe.workout_stage_id = ws.id
                JOIN programmed_workout pw ON ws.programmed_workout_id = pw.id
                JOIN program p ON pw.program_id = p.id
                WHERE p.user_id = $1
                ORDER BY ss.programmed_exercise_id, ss.set_number
                """.trimIndent(),
                userId
            )
        ).thenReturn(Mono.error(databaseError))

        val result = setSchemeDAL.selectSetSchemesByUserId(userId)

        StepVerifier.create(result).expectError(databaseError::class.java).verify()
        verify(postgresClient).select<SetScheme>(
            """
            SELECT ss.*
            FROM set_scheme ss
            JOIN programmed_exercise pe ON ss.programmed_exercise_id = pe.id
            JOIN workout_stage ws ON pe.workout_stage_id = ws.id
            JOIN programmed_workout pw ON ws.programmed_workout_id = pw.id
            JOIN program p ON pw.program_id = p.id
            WHERE p.user_id = $1
            ORDER BY ss.programmed_exercise_id, ss.set_number
            """.trimIndent(),
            userId
        )
    }
}

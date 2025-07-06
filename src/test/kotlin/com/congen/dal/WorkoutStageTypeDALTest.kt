package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.WorkoutStageType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class WorkoutStageTypeDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var workoutStageTypeDAL: WorkoutStageTypeDAL

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        workoutStageTypeDAL = WorkoutStageTypeDAL(postgresClient)
    }

    @Test
    fun `selectWorkoutStageTypeById should return workout stage type`() {
        val workoutStageType =
            WorkoutStageType(
                id = 1,
                name = "Warm-up"
            )

        whenever(
            postgresClient.selectIndividual<WorkoutStageType>(
                "SELECT * FROM workout_stage_type WHERE id=\$1",
                1
            )
        ).thenReturn(Mono.just(workoutStageType))

        val result = workoutStageTypeDAL.selectWorkoutStageTypeById(1)

        StepVerifier.create(result)
            .expectNext(workoutStageType)
            .verifyComplete()

        verify(postgresClient).selectIndividual<WorkoutStageType>(
            "SELECT * FROM workout_stage_type WHERE id=\$1",
            1
        )
    }

    @Test
    fun `selectWorkoutStageTypeByName should return workout stage type`() {
        val workoutStageType =
            WorkoutStageType(
                id = 1,
                name = "Warm-up"
            )

        whenever(
            postgresClient.selectIndividual<WorkoutStageType>(
                "SELECT * FROM workout_stage_type WHERE name=\$1",
                "Warm-up"
            )
        ).thenReturn(Mono.just(workoutStageType))

        val result = workoutStageTypeDAL.selectWorkoutStageTypeByName("Warm-up")

        StepVerifier.create(result)
            .expectNext(workoutStageType)
            .verifyComplete()

        verify(postgresClient).selectIndividual<WorkoutStageType>(
            "SELECT * FROM workout_stage_type WHERE name=\$1",
            "Warm-up"
        )
    }

    @Test
    fun `selectWorkoutStageTypes should return all workout stage types`() {
        val workoutStageTypes =
            listOf(
                WorkoutStageType(
                    id = 1,
                    name = "Warm-up"
                ),
                WorkoutStageType(
                    id = 2,
                    name = "Main"
                ),
                WorkoutStageType(
                    id = 3,
                    name = "Cool-down"
                )
            )

        whenever(
            postgresClient.select<WorkoutStageType>("SELECT * FROM workout_stage_type ORDER BY name")
        ).thenReturn(Mono.just(workoutStageTypes))

        val result = workoutStageTypeDAL.selectWorkoutStageTypes()

        StepVerifier.create(result)
            .expectNext(workoutStageTypes)
            .verifyComplete()

        verify(postgresClient).select<WorkoutStageType>("SELECT * FROM workout_stage_type ORDER BY name")
    }
} 

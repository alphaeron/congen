package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.WorkoutStageType
import com.congen.model.WorkoutStageTypeEnum
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class WorkoutStageTypeDALTest {
    @Mock
    private lateinit var postgresClient: PostgresClient

    private lateinit var workoutStageTypeDAL: WorkoutStageTypeDAL

    @BeforeEach
    fun setUp() {
        workoutStageTypeDAL = WorkoutStageTypeDAL(postgresClient)
    }

    @Test
    fun `selectWorkoutStageTypeById should return workout stage type when found`() {
        // Given
        val workoutStageType =
            WorkoutStageType(
                id = 1,
                name = WorkoutStageTypeEnum.PRIMARY,
                createdAt = LocalDateTime.now()
            )

        whenever(postgresClient.selectIndividual<WorkoutStageType>("SELECT * FROM workout_stage_type WHERE id=$1", 1))
            .thenReturn(Mono.just(workoutStageType))

        // When
        val result = workoutStageTypeDAL.selectWorkoutStageTypeById(1)

        // Then
        StepVerifier.create(result)
            .expectNext(workoutStageType)
            .verifyComplete()

        verify(postgresClient).selectIndividual<WorkoutStageType>("SELECT * FROM workout_stage_type WHERE id=$1", 1)
    }

    @Test
    fun `selectWorkoutStageTypeByEnum should return workout stage type when found`() {
        // Given
        val workoutStageType =
            WorkoutStageType(
                id = 1,
                name = WorkoutStageTypeEnum.PRIMARY,
                createdAt = LocalDateTime.now()
            )

        whenever(
            postgresClient.selectIndividual<WorkoutStageType>(
                "SELECT * FROM workout_stage_type WHERE name=$1",
                WorkoutStageTypeEnum.PRIMARY.displayName
            )
        )
            .thenReturn(Mono.just(workoutStageType))

        // When
        val result = workoutStageTypeDAL.selectWorkoutStageTypeByEnum(WorkoutStageTypeEnum.PRIMARY)

        // Then
        StepVerifier.create(result)
            .expectNext(workoutStageType)
            .verifyComplete()

        verify(
            postgresClient
        ).selectIndividual<WorkoutStageType>("SELECT * FROM workout_stage_type WHERE name=$1", WorkoutStageTypeEnum.PRIMARY.displayName)
    }

    @Test
    fun `selectWorkoutStageTypes should return all workout stage types`() {
        // Given
        val workoutStageTypes =
            listOf(
                WorkoutStageType(
                    id = 1,
                    name = WorkoutStageTypeEnum.PRIMARY,
                    createdAt = LocalDateTime.now()
                ),
                WorkoutStageType(
                    id = 2,
                    name = WorkoutStageTypeEnum.SECONDARY,
                    createdAt = LocalDateTime.now()
                )
            )

        whenever(postgresClient.select<WorkoutStageType>("SELECT * FROM workout_stage_type ORDER BY name"))
            .thenReturn(Mono.just(workoutStageTypes))

        // When
        val result = workoutStageTypeDAL.selectWorkoutStageTypes()

        // Then
        StepVerifier.create(result)
            .expectNext(workoutStageTypes)
            .verifyComplete()

        verify(postgresClient).select<WorkoutStageType>("SELECT * FROM workout_stage_type ORDER BY name")
    }
}

package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.mockWorkoutStageType
import com.congen.model.WorkoutStageType
import com.congen.model.WorkoutStageTypeEnum
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

    private val workoutStageType = mockWorkoutStageType()
    private val workoutStageTypes =
        listOf(
            mockWorkoutStageType(name = WorkoutStageTypeEnum.PRIMARY),
            mockWorkoutStageType(name = WorkoutStageTypeEnum.SECONDARY)
        )

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        workoutStageTypeDAL = WorkoutStageTypeDAL(postgresClient)
    }

    @Test
    fun `selectWorkoutStageTypeById should return workout stage type when found`() {
        whenever(postgresClient.selectIndividual<WorkoutStageType>("SELECT * FROM workout_stage_type WHERE id=$1", workoutStageType.id))
            .thenReturn(Mono.just(workoutStageType))

        val result = workoutStageTypeDAL.selectWorkoutStageTypeById(workoutStageType.id)

        StepVerifier.create(result)
            .expectNext(workoutStageType)
            .verifyComplete()
        verify(postgresClient).selectIndividual<WorkoutStageType>("SELECT * FROM workout_stage_type WHERE id=$1", workoutStageType.id)
    }

    @Test
    fun `selectWorkoutStageTypeByEnum should return workout stage type when found`() {
        whenever(
            postgresClient.selectIndividual<WorkoutStageType>(
                "SELECT * FROM workout_stage_type WHERE name=$1",
                WorkoutStageTypeEnum.PRIMARY.displayName
            )
        )
            .thenReturn(Mono.just(workoutStageType))

        val result = workoutStageTypeDAL.selectWorkoutStageTypeByEnum(WorkoutStageTypeEnum.PRIMARY)

        StepVerifier.create(result)
            .expectNext(workoutStageType)
            .verifyComplete()
        verify(
            postgresClient
        ).selectIndividual<WorkoutStageType>("SELECT * FROM workout_stage_type WHERE name=$1", WorkoutStageTypeEnum.PRIMARY.displayName)
    }

    @Test
    fun `selectWorkoutStageTypes should return all workout stage types`() {
        whenever(postgresClient.select<WorkoutStageType>("SELECT * FROM workout_stage_type ORDER BY name"))
            .thenReturn(Mono.just(workoutStageTypes))

        val result = workoutStageTypeDAL.selectWorkoutStageTypes()

        StepVerifier.create(result)
            .expectNext(workoutStageTypes)
            .verifyComplete()
        verify(postgresClient).select<WorkoutStageType>("SELECT * FROM workout_stage_type ORDER BY name")
    }
}

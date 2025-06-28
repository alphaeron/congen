package com.congen

import com.congen.controllers.EquipmentController
import com.congen.controllers.ExerciseController
import com.congen.controllers.ExerciseEquipmentController
import com.congen.controllers.ExerciseMuscleController
import com.congen.controllers.MuscleController
import com.congen.dal.EquipmentDAL
import com.congen.dal.ExerciseDAL
import com.congen.dal.ExerciseEquipmentDAL
import com.congen.dal.ExerciseMuscleDAL
import com.congen.dal.MuscleDAL
import com.congen.model.Equipment
import com.congen.model.Exercise
import com.congen.model.ExerciseEquipment
import com.congen.model.ExerciseMuscle
import com.congen.model.Muscle
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono

@WebFluxTest(controllers = [
	EquipmentController::class,
	ExerciseController::class,
	MuscleController::class,
	ExerciseEquipmentController::class,
	ExerciseMuscleController::class
])
class CongenApplicationTests {

	@Autowired
	private lateinit var webTestClient: WebTestClient

	@MockBean
	private lateinit var equipmentDAL: EquipmentDAL

	@MockBean
	private lateinit var exerciseDAL: ExerciseDAL

	@MockBean
	private lateinit var muscleDAL: MuscleDAL

	@MockBean
	private lateinit var exerciseEquipmentDAL: ExerciseEquipmentDAL

	@MockBean
	private lateinit var exerciseMuscleDAL: ExerciseMuscleDAL

	@Test
	fun `should return 404 for non-existent endpoints`() {
		webTestClient.get()
			.uri("/non-existent")
			.exchange()
			.expectStatus().isNotFound
	}

	@Test
	fun `should handle equipment save endpoint`() {
		val equipment = Equipment(
			name = "Test Barbell",
			description = "A test barbell for unit testing"
		)

		`when`(equipmentDAL.insertEquipment(equipment)).thenReturn(Mono.just(equipment))

		webTestClient.post()
			.uri("/equipment/")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(equipment)
			.exchange()
			.expectStatus().isOk
			.expectBody()
			.jsonPath("$.name").isEqualTo("Test Barbell")
			.jsonPath("$.description").isEqualTo("A test barbell for unit testing")
	}

	@Test
	fun `should handle equipment get all endpoint`() {
		val equipmentList = listOf(
			Equipment(name = "Barbell", description = "A barbell"),
			Equipment(name = "Dumbbell", description = "A dumbbell")
		)

		`when`(equipmentDAL.selectEquipment()).thenReturn(Mono.just(equipmentList))

		webTestClient.get()
			.uri("/equipment/")
			.exchange()
			.expectStatus().isOk
			.expectBodyList(Equipment::class.java)
			.hasSize(2)
			.contains(equipmentList[0], equipmentList[1])
	}

	@Test
	fun `should handle equipment get by name endpoint - found`() {
		val equipment = Equipment(name = "Test Barbell", description = "A test barbell")
		`when`(equipmentDAL.selectEquipmentByName("Test Barbell")).thenReturn(Mono.just(equipment))

		webTestClient.get()
			.uri("/equipment/Test Barbell")
			.exchange()
			.expectStatus().isOk
			.expectBody(Equipment::class.java)
			.isEqualTo(equipment)
	}

	@Test
	fun `should handle equipment get by name endpoint - not found`() {
		`when`(equipmentDAL.selectEquipmentByName("NonExistent")).thenReturn(Mono.empty())

		webTestClient.get()
			.uri("/equipment/NonExistent")
			.exchange()
			.expectStatus().isNotFound
	}

	@Test
	fun `should handle equipment exercise endpoint`() {
		val exerciseEquipmentList = listOf(
			ExerciseEquipment(exerciseName = "Bench Press", equipmentName = "Barbell")
		)

		`when`(exerciseEquipmentDAL.selectExerciseEquipmentByEquipment("Barbell"))
			.thenReturn(Mono.just(exerciseEquipmentList))

		webTestClient.get()
			.uri("/equipment/Barbell/exercise")
			.exchange()
			.expectStatus().isOk
			.expectBodyList(ExerciseEquipment::class.java)
			.hasSize(1)
			.contains(exerciseEquipmentList[0])
	}

	@Test
	fun `should handle exercise save endpoint`() {
		val exercise = Exercise(
			name = "Test Bench Press",
			description = "A test bench press exercise",
			movementType = "push",
			isUnilateral = false,
			isUpper = true,
			isAccessory = false
		)

		`when`(exerciseDAL.insertExercise(exercise)).thenReturn(Mono.just(exercise))

		webTestClient.post()
			.uri("/exercise/")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(exercise)
			.exchange()
			.expectStatus().isOk
			.expectBody()
			.jsonPath("$.name").isEqualTo("Test Bench Press")
			.jsonPath("$.description").isEqualTo("A test bench press exercise")
			.jsonPath("$.movement_type").isEqualTo("push")
	}

	@Test
	fun `should handle exercise get all endpoint`() {
		val exerciseList = listOf(
			Exercise(
				name = "Bench Press",
				description = "A bench press exercise",
				movementType = "push",
				isUnilateral = false,
				isUpper = true,
				isAccessory = false
			),
			Exercise(
				name = "Squat",
				description = "A squat exercise",
				movementType = "push",
				isUnilateral = false,
				isUpper = false,
				isAccessory = false
			)
		)

		`when`(exerciseDAL.selectExercises()).thenReturn(Mono.just(exerciseList))

		webTestClient.get()
			.uri("/exercise/")
			.exchange()
			.expectStatus().isOk
			.expectBodyList(Exercise::class.java)
			.hasSize(2)
			.contains(exerciseList[0], exerciseList[1])
	}

	@Test
	fun `should handle exercise get by name endpoint - found`() {
		val exercise = Exercise(
			name = "Test Bench Press",
			description = "A test bench press exercise",
			movementType = "push",
			isUnilateral = false,
			isUpper = true,
			isAccessory = false
		)

		`when`(exerciseDAL.selectExerciseByName("Test Bench Press")).thenReturn(Mono.just(exercise))

		webTestClient.get()
			.uri("/exercise/Test Bench Press")
			.exchange()
			.expectStatus().isOk
			.expectBody(Exercise::class.java)
			.isEqualTo(exercise)
	}

	@Test
	fun `should handle exercise get by name endpoint - not found`() {
		`when`(exerciseDAL.selectExerciseByName("NonExistent")).thenReturn(Mono.empty())

		webTestClient.get()
			.uri("/exercise/NonExistent")
			.exchange()
			.expectStatus().isNotFound
	}

	@Test
	fun `should handle exercise muscle endpoint`() {
		val exerciseMuscleList = listOf(
			ExerciseMuscle(exerciseName = "Bench Press", muscleName = "Chest")
		)

		`when`(exerciseMuscleDAL.selectExerciseMuscleByExercise("Bench Press"))
			.thenReturn(Mono.just(exerciseMuscleList))

		webTestClient.get()
			.uri("/exercise/Bench Press/muscle")
			.exchange()
			.expectStatus().isOk
			.expectBodyList(ExerciseMuscle::class.java)
			.hasSize(1)
			.contains(exerciseMuscleList[0])
	}

	@Test
	fun `should handle exercise equipment endpoint`() {
		val exerciseEquipmentList = listOf(
			ExerciseEquipment(exerciseName = "Bench Press", equipmentName = "Barbell")
		)

		`when`(exerciseEquipmentDAL.selectExerciseEquipmentByExercise("Bench Press"))
			.thenReturn(Mono.just(exerciseEquipmentList))

		webTestClient.get()
			.uri("/exercise/Bench Press/equipment")
			.exchange()
			.expectStatus().isOk
			.expectBodyList(ExerciseEquipment::class.java)
			.hasSize(1)
			.contains(exerciseEquipmentList[0])
	}

	@Test
	fun `should handle muscle save endpoint`() {
		val muscle = Muscle(
			name = "Test Chest",
			description = "Test chest muscles"
		)

		`when`(muscleDAL.insertMuscle(muscle)).thenReturn(Mono.just(muscle))

		webTestClient.post()
			.uri("/muscle/")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(muscle)
			.exchange()
			.expectStatus().isOk
			.expectBody()
			.jsonPath("$.name").isEqualTo("Test Chest")
			.jsonPath("$.description").isEqualTo("Test chest muscles")
	}

	@Test
	fun `should handle muscle get all endpoint`() {
		val muscleList = listOf(
			Muscle(name = "Chest", description = "Chest muscles"),
			Muscle(name = "Back", description = "Back muscles")
		)

		`when`(muscleDAL.selectMuscles()).thenReturn(Mono.just(muscleList))

		webTestClient.get()
			.uri("/muscle/")
			.exchange()
			.expectStatus().isOk
			.expectBodyList(Muscle::class.java)
			.hasSize(2)
			.contains(muscleList[0], muscleList[1])
	}

	@Test
	fun `should handle muscle get by name endpoint - found`() {
		val muscle = Muscle(name = "Test Chest", description = "Test chest muscles")
		`when`(muscleDAL.selectMuscleByName("Test Chest")).thenReturn(Mono.just(muscle))

		webTestClient.get()
			.uri("/muscle/Test Chest")
			.exchange()
			.expectStatus().isOk
			.expectBody(Muscle::class.java)
			.isEqualTo(muscle)
	}

	@Test
	fun `should handle muscle get by name endpoint - not found`() {
		`when`(muscleDAL.selectMuscleByName("NonExistent")).thenReturn(Mono.empty())

		webTestClient.get()
			.uri("/muscle/NonExistent")
			.exchange()
			.expectStatus().isNotFound
	}

	@Test
	fun `should handle muscle exercise endpoint`() {
		val exerciseMuscleList = listOf(
			ExerciseMuscle(exerciseName = "Bench Press", muscleName = "Chest")
		)

		`when`(exerciseMuscleDAL.selectExerciseMuscleByMuscle("Chest"))
			.thenReturn(Mono.just(exerciseMuscleList))

		webTestClient.get()
			.uri("/muscle/Chest/exercise")
			.exchange()
			.expectStatus().isOk
			.expectBodyList(ExerciseMuscle::class.java)
			.hasSize(1)
			.contains(exerciseMuscleList[0])
	}

	@Test
	fun `should handle exercise equipment save endpoint`() {
		val exerciseEquipment = ExerciseEquipment(
			exerciseName = "Test Bench Press",
			equipmentName = "Test Barbell"
		)

		`when`(exerciseEquipmentDAL.insertExerciseEquipment(exerciseEquipment))
			.thenReturn(Mono.just(exerciseEquipment))

		webTestClient.post()
			.uri("/exercise_equipment/")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(exerciseEquipment)
			.exchange()
			.expectStatus().isOk
			.expectBody()
			.jsonPath("$.exercise_name").isEqualTo("Test Bench Press")
			.jsonPath("$.equipment_name").isEqualTo("Test Barbell")
	}

	@Test
	fun `should handle exercise equipment get all endpoint`() {
		val exerciseEquipmentList = listOf(
			ExerciseEquipment(exerciseName = "Bench Press", equipmentName = "Barbell")
		)

		`when`(exerciseEquipmentDAL.selectAllExerciseEquipment()).thenReturn(Mono.just(exerciseEquipmentList))

		webTestClient.get()
			.uri("/exercise_equipment/")
			.exchange()
			.expectStatus().isOk
			.expectBodyList(ExerciseEquipment::class.java)
			.hasSize(1)
			.contains(exerciseEquipmentList[0])
	}

	@Test
	fun `should handle exercise muscle save endpoint`() {
		val exerciseMuscle = ExerciseMuscle(
			exerciseName = "Test Bench Press",
			muscleName = "Test Chest"
		)

		`when`(exerciseMuscleDAL.insertExerciseMuscle(exerciseMuscle))
			.thenReturn(Mono.just(exerciseMuscle))

		webTestClient.post()
			.uri("/exercise_muscle/")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(exerciseMuscle)
			.exchange()
			.expectStatus().isOk
			.expectBody()
			.jsonPath("$.exercise_name").isEqualTo("Test Bench Press")
			.jsonPath("$.muscle_name").isEqualTo("Test Chest")
	}

	@Test
	fun `should handle exercise muscle get all endpoint`() {
		val exerciseMuscleList = listOf(
			ExerciseMuscle(exerciseName = "Bench Press", muscleName = "Chest")
		)

		`when`(exerciseMuscleDAL.selectAllExerciseMuscle()).thenReturn(Mono.just(exerciseMuscleList))

		webTestClient.get()
			.uri("/exercise_muscle/")
			.exchange()
			.expectStatus().isOk
			.expectBodyList(ExerciseMuscle::class.java)
			.hasSize(1)
			.contains(exerciseMuscleList[0])
	}

	@Test
	fun `should handle invalid JSON gracefully`() {
		webTestClient.post()
			.uri("/equipment/")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue("invalid json")
			.exchange()
			.expectStatus().isBadRequest
	}

	@Test
	fun `should handle missing required fields gracefully`() {
		val invalidEquipment = mapOf("description" to "Missing name field")
		
		webTestClient.post()
			.uri("/equipment/")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(invalidEquipment)
			.exchange()
			.expectStatus().isBadRequest
	}
}

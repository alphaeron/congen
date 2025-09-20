package com.congen.controllers

import com.congen.client.PostgresClient
import com.congen.createGdprComplianceServiceSpy
import com.congen.dal.UserEquipmentDAL
import com.congen.exceptions.DatabaseQueryException
import com.congen.mockUserEquipment
import com.congen.service.GdprComplianceService
import com.congen.util.KeycloakUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import org.springframework.test.context.TestPropertySource
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant

/**
 * Unit tests for UserEquipmentController.
 *
 * These tests verify the REST API endpoints for user equipment operations,
 * including CRUD operations and error handling.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@TestPropertySource(
    properties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"]
)
class UserEquipmentControllerTest {
    private lateinit var userEquipmentDAL: UserEquipmentDAL
    private lateinit var keycloakUtil: KeycloakUtil
    private lateinit var gdprComplianceService: GdprComplianceService
    private lateinit var userEquipmentController: UserEquipmentController

    companion object {
        private const val USER_ID = "b226d772-c063-4974-ae08-ab64134abbcf"
        private const val EQUIPMENT_NAME = "Barbell"
        private const val DUMBBELLS = "Dumbbells"
    }

    @BeforeEach
    fun setUp() {
        userEquipmentDAL = mock()
        keycloakUtil = mock()
        gdprComplianceService = createGdprComplianceServiceSpy()
        val postgresClient = mock<PostgresClient>()

        // Mock PostgresClient.withTransaction to execute the block directly
        doAnswer { invocation ->
            val block = invocation.getArgument<() -> Mono<ResponseEntity<List<com.congen.model.UserEquipment>>>>(0)
            block.invoke()
        }.whenever(postgresClient).withTransaction(any<() -> Mono<ResponseEntity<List<com.congen.model.UserEquipment>>>>())

        userEquipmentController = UserEquipmentController(userEquipmentDAL, keycloakUtil, gdprComplianceService, postgresClient)

        // Mock KeycloakUtil methods for all tests
        doReturn(Mono.just(USER_ID)).whenever(keycloakUtil).getCurrentUserId()
        doReturn(Mono.just(setOf("user"))).whenever(keycloakUtil).getCurrentUserRoles()

        // Mock GDPR compliance service for all tests
        doReturn(Mono.just(true)).whenever(gdprComplianceService).hasUserConsent(any<String>())
    }

    @Test
    fun `save should return created user equipment`() {
        val now = Instant.now()
        val userEquipment = mockUserEquipment(userId = USER_ID, equipmentName = EQUIPMENT_NAME, createdAt = now)
        whenever(userEquipmentDAL.insertUserEquipment(USER_ID, EQUIPMENT_NAME)).thenReturn(Mono.just(userEquipment))
        val result = userEquipmentController.save(USER_ID, EQUIPMENT_NAME)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(userEquipment))
            .verifyComplete()
        verify(userEquipmentDAL).insertUserEquipment(USER_ID, EQUIPMENT_NAME)
    }

    @Test
    fun `save should handle duplicate key error with 409 status`() {
        val databaseException = DatabaseQueryException("duplicate key value violates unique constraint")
        whenever(userEquipmentDAL.insertUserEquipment(USER_ID, EQUIPMENT_NAME))
            .thenReturn(Mono.error(databaseException))
        val result = userEquipmentController.save(USER_ID, EQUIPMENT_NAME)

        StepVerifier.create(result)
            .expectError(DatabaseQueryException::class.java)
            .verify()
    }

    @Test
    fun `save should handle foreign key violation error with 422 status`() {
        val databaseException = DatabaseQueryException("violates foreign key constraint")
        whenever(userEquipmentDAL.insertUserEquipment(USER_ID, EQUIPMENT_NAME))
            .thenReturn(Mono.error(databaseException))
        val result = userEquipmentController.save(USER_ID, EQUIPMENT_NAME)

        StepVerifier.create(result)
            .expectError(DatabaseQueryException::class.java)
            .verify()
    }

    @Test
    fun `save should propagate other database errors`() {
        val databaseException = DatabaseQueryException("Database connection failed")
        whenever(userEquipmentDAL.insertUserEquipment(USER_ID, EQUIPMENT_NAME))
            .thenReturn(Mono.error(databaseException))
        val result = userEquipmentController.save(USER_ID, EQUIPMENT_NAME)

        StepVerifier.create(result)
            .expectError(DatabaseQueryException::class.java)
            .verify()
    }

    @Test
    fun `getByUser should return user equipment when found`() {
        val now = Instant.now()
        val userEquipment = mockUserEquipment(userId = USER_ID, equipmentName = EQUIPMENT_NAME, createdAt = now)
        val userEquipmentList =
            listOf(
                userEquipment,
                mockUserEquipment(userId = USER_ID, equipmentName = DUMBBELLS, createdAt = now)
            )
        whenever(userEquipmentDAL.selectUserEquipmentByUser(USER_ID)).thenReturn(Mono.just(userEquipmentList))
        val result = userEquipmentController.getByUser(USER_ID)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(userEquipmentList))
            .verifyComplete()
        verify(userEquipmentDAL).selectUserEquipmentByUser(USER_ID)
    }

    @Test
    fun `getByUser should handle database errors`() {
        whenever(userEquipmentDAL.selectUserEquipmentByUser(USER_ID))
            .thenReturn(Mono.error(DatabaseQueryException("Database error")))
        val result = userEquipmentController.getByUser(USER_ID)
        StepVerifier.create(result)
            .expectError(DatabaseQueryException::class.java)
            .verify()
    }

    @Test
    fun `delete should return deleted user equipment`() {
        val now = Instant.now()
        val userEquipment = mockUserEquipment(userId = USER_ID, equipmentName = EQUIPMENT_NAME, createdAt = now)
        whenever(userEquipmentDAL.deleteUserEquipment(USER_ID, EQUIPMENT_NAME)).thenReturn(Mono.just(userEquipment))
        val result = userEquipmentController.delete(USER_ID, EQUIPMENT_NAME)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(userEquipment))
            .verifyComplete()
        verify(userEquipmentDAL).deleteUserEquipment(USER_ID, EQUIPMENT_NAME)
    }

    @Test
    fun `delete should handle duplicate key error with 409 status`() {
        val databaseException = DatabaseQueryException("duplicate key value violates unique constraint")
        whenever(userEquipmentDAL.deleteUserEquipment(USER_ID, EQUIPMENT_NAME))
            .thenReturn(Mono.error(databaseException))
        val result = userEquipmentController.delete(USER_ID, EQUIPMENT_NAME)

        StepVerifier.create(result)
            .expectError(DatabaseQueryException::class.java)
            .verify()
    }

    @Test
    fun `delete should handle foreign key violation error with 422 status`() {
        val databaseException = DatabaseQueryException("violates foreign key constraint")
        whenever(userEquipmentDAL.deleteUserEquipment(USER_ID, EQUIPMENT_NAME))
            .thenReturn(Mono.error(databaseException))
        val result = userEquipmentController.delete(USER_ID, EQUIPMENT_NAME)

        StepVerifier.create(result)
            .expectError(DatabaseQueryException::class.java)
            .verify()
    }

    @Test
    fun `delete should propagate other database errors`() {
        val databaseException = DatabaseQueryException("Database connection failed")
        whenever(userEquipmentDAL.deleteUserEquipment(USER_ID, EQUIPMENT_NAME))
            .thenReturn(Mono.error(databaseException))
        val result = userEquipmentController.delete(USER_ID, EQUIPMENT_NAME)

        StepVerifier.create(result)
            .expectError(DatabaseQueryException::class.java)
            .verify()
    }

    @Test
    fun `saveBulk should return created user equipment list`() {
        val now = Instant.now()
        val equipmentNames = listOf(EQUIPMENT_NAME, DUMBBELLS)
        val userEquipment1 = mockUserEquipment(userId = USER_ID, equipmentName = EQUIPMENT_NAME, createdAt = now)
        val userEquipment2 = mockUserEquipment(userId = USER_ID, equipmentName = DUMBBELLS, createdAt = now)
        val expectedList = listOf(userEquipment1, userEquipment2)

        whenever(userEquipmentDAL.insertUserEquipment(USER_ID, EQUIPMENT_NAME)).thenReturn(Mono.just(userEquipment1))
        whenever(userEquipmentDAL.insertUserEquipment(USER_ID, DUMBBELLS)).thenReturn(Mono.just(userEquipment2))

        val result = userEquipmentController.saveBulk(USER_ID, equipmentNames)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(expectedList))
            .verifyComplete()

        verify(userEquipmentDAL).insertUserEquipment(USER_ID, EQUIPMENT_NAME)
        verify(userEquipmentDAL).insertUserEquipment(USER_ID, DUMBBELLS)
    }

    @Test
    fun `saveBulk should handle duplicate key error with 409 status`() {
        val equipmentNames = listOf(EQUIPMENT_NAME, DUMBBELLS)
        val databaseException = DatabaseQueryException("duplicate key value violates unique constraint")
        whenever(userEquipmentDAL.insertUserEquipment(USER_ID, EQUIPMENT_NAME))
            .thenReturn(Mono.error(databaseException))

        val result = userEquipmentController.saveBulk(USER_ID, equipmentNames)
        StepVerifier.create(result)
            .expectError(DatabaseQueryException::class.java)
            .verify()
    }

    @Test
    fun `saveBulk should handle foreign key violation error with 422 status`() {
        val equipmentNames = listOf(EQUIPMENT_NAME, DUMBBELLS)
        val databaseException = DatabaseQueryException("violates foreign key constraint")
        whenever(userEquipmentDAL.insertUserEquipment(USER_ID, EQUIPMENT_NAME))
            .thenReturn(Mono.error(databaseException))

        val result = userEquipmentController.saveBulk(USER_ID, equipmentNames)
        StepVerifier.create(result)
            .expectError(DatabaseQueryException::class.java)
            .verify()
    }

    @Test
    fun `saveBulk should propagate other database errors`() {
        val equipmentNames = listOf(EQUIPMENT_NAME, DUMBBELLS)
        val databaseException = DatabaseQueryException("Database connection failed")
        whenever(userEquipmentDAL.insertUserEquipment(USER_ID, EQUIPMENT_NAME))
            .thenReturn(Mono.error(databaseException))

        val result = userEquipmentController.saveBulk(USER_ID, equipmentNames)
        StepVerifier.create(result)
            .expectError(DatabaseQueryException::class.java)
            .verify()
    }
}

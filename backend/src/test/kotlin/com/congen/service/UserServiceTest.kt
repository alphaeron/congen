package com.congen.service

import com.congen.client.KeycloakClient
import com.congen.dal.UserDAL
import com.congen.exceptions.DatabaseException
import com.congen.exceptions.ValidationException
import com.congen.mockUser
import com.congen.model.WeightUnit
import com.congen.util.UnitConverter
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.Instant
import kotlin.test.assertEquals

/**
 * Unit tests for UserService.
 *
 * These tests verify the business logic for user operations,
 * including validation, conversion, and DAL interactions.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension::class)
class UserServiceTest {
    private lateinit var userService: UserService
    private lateinit var userDAL: UserDAL
    private lateinit var unitConverter: UnitConverter
    private lateinit var keycloakClient: KeycloakClient

    companion object {
        private const val USER_ID = 1
        private const val USER_ID_2 = 2
        private const val NAME = "John Doe"
        private const val JANE_NAME = "Jane Smith"
        private const val AGE = 30
        private const val JANE_AGE = 25
        private const val HEIGHT = "180.5"
        private const val JANE_HEIGHT = "165.0"
        private const val WEIGHT = "75.0"
        private const val JANE_WEIGHT = "60.0"
    }

    @BeforeEach
    fun setUp() {
        userDAL = mock()
        unitConverter = mock()
        keycloakClient = mock()
        userService = UserService(userDAL, unitConverter, keycloakClient)
    }

    @Test
    fun `createUser should create user successfully`() {
        val now = Instant.now()
        val user =
            mockUser(
                id = 0,
                name = NAME,
                age = AGE,
                height = BigDecimal(HEIGHT),
                weight = BigDecimal(WEIGHT),
                createdAt = now,
                updatedAt = now
            )
        val savedUser = user.copy(id = USER_ID)

        // Mock unit conversion for KG (no conversion needed)
        whenever(unitConverter.toKg(eq(BigDecimal(WEIGHT)), eq(WeightUnit.KG)))
            .thenReturn(BigDecimal(WEIGHT))
        // Mock Keycloak client to return a user ID
        whenever(keycloakClient.createUser(eq("test@example.com"), eq("test@example.com"), eq("John"), eq("Doe"), eq("password123")))
            .thenReturn(Mono.just("keycloak-user-id"))
        whenever(userDAL.insertUser(eq(NAME), eq(AGE), eq(BigDecimal(HEIGHT)), eq(BigDecimal(WEIGHT)), eq("keycloak-user-id")))
            .thenReturn(Mono.just(savedUser))

        val result = userService.createUser(NAME, AGE, BigDecimal(HEIGHT), BigDecimal(WEIGHT), "KG", "test@example.com", "password123")

        StepVerifier.create(result)
            .expectNext(savedUser)
            .verifyComplete()

        verify(userDAL).insertUser(NAME, AGE, BigDecimal(HEIGHT), BigDecimal(WEIGHT), "keycloak-user-id")
    }

    @Test
    fun `createUser should convert weight from LBS to KG`() {
        val now = Instant.now()
        val weightInLbs = BigDecimal("150.0")
        val weightInKg = BigDecimal("68.04")
        val user =
            mockUser(
                id = 0,
                name = NAME,
                age = AGE,
                height = BigDecimal(HEIGHT),
                weight = weightInKg,
                createdAt = now,
                updatedAt = now
            )
        val savedUser = user.copy(id = USER_ID)

        whenever(unitConverter.toKg(eq(weightInLbs), eq(WeightUnit.LBS)))
            .thenReturn(weightInKg)
        // Mock Keycloak client to return a user ID
        whenever(keycloakClient.createUser(eq("test@example.com"), eq("test@example.com"), eq("John"), eq("Doe"), eq("password123")))
            .thenReturn(Mono.just("keycloak-user-id"))
        whenever(userDAL.insertUser(eq(NAME), eq(AGE), eq(BigDecimal(HEIGHT)), eq(weightInKg), eq("keycloak-user-id")))
            .thenReturn(Mono.just(savedUser))

        val result = userService.createUser(NAME, AGE, BigDecimal(HEIGHT), weightInLbs, "LBS", "test@example.com", "password123")

        StepVerifier.create(result)
            .expectNext(savedUser)
            .verifyComplete()

        verify(unitConverter).toKg(weightInLbs, WeightUnit.LBS)
        verify(userDAL).insertUser(NAME, AGE, BigDecimal(HEIGHT), weightInKg, "keycloak-user-id")
    }

    @Test
    fun `createUser should throw ValidationException when validation fails`() {
        val invalidWeight = BigDecimal("-10.0")
        val convertedWeight = BigDecimal("-10.0") // Invalid weight after conversion

        whenever(unitConverter.toKg(eq(invalidWeight), eq(WeightUnit.KG)))
            .thenReturn(convertedWeight)

        val result = userService.createUser(NAME, AGE, BigDecimal(HEIGHT), invalidWeight, "KG", "test@example.com", "password123")

        StepVerifier.create(result)
            .expectErrorSatisfies { ex ->
                assert(ex is ValidationException)
                assertEquals("User weight must be between 0.01 and 1000 kg, got: -10.0", ex.message)
            }
            .verify()
    }

    @Test
    fun `getUserById should return user when found`() {
        val now = Instant.now()
        val user =
            mockUser(
                id = USER_ID,
                name = NAME,
                age = AGE,
                height = BigDecimal(HEIGHT),
                weight = BigDecimal(WEIGHT),
                createdAt = now,
                updatedAt = now
            )

        whenever(userDAL.selectUserById(USER_ID)).thenReturn(Mono.just(user))

        val result = userService.getUserById(USER_ID)

        StepVerifier.create(result)
            .expectNext(user)
            .verifyComplete()

        verify(userDAL).selectUserById(USER_ID)
    }

    @Test
    fun `getUserById should propagate error when user not found`() {
        val error = DatabaseException("User not found")
        whenever(userDAL.selectUserById(USER_ID)).thenReturn(Mono.error(error))

        val result = userService.getUserById(USER_ID)

        StepVerifier.create(result)
            .expectErrorSatisfies { ex ->
                assert(ex is DatabaseException)
                assertEquals("User not found", ex.message)
            }
            .verify()

        verify(userDAL).selectUserById(USER_ID)
    }

    @Test
    fun `getAllUsers should return all users`() {
        val now = Instant.now()
        val users =
            listOf(
                mockUser(
                    id = USER_ID,
                    name = NAME,
                    age = AGE,
                    height = BigDecimal(HEIGHT),
                    weight = BigDecimal(WEIGHT),
                    createdAt = now,
                    updatedAt = now
                ),
                mockUser(
                    id = USER_ID_2,
                    name = JANE_NAME,
                    age = JANE_AGE,
                    height = BigDecimal(JANE_HEIGHT),
                    weight = BigDecimal(JANE_WEIGHT),
                    createdAt = now,
                    updatedAt = now
                )
            )

        whenever(userDAL.selectUsers()).thenReturn(Mono.just(users))

        val result = userService.getAllUsers()

        StepVerifier.create(result)
            .expectNext(users)
            .verifyComplete()

        verify(userDAL).selectUsers()
    }

    @Test
    fun `updateUser should update user successfully`() {
        val now = Instant.now()
        val user =
            mockUser(
                id = USER_ID,
                name = NAME,
                age = AGE,
                height = BigDecimal(HEIGHT),
                weight = BigDecimal(WEIGHT),
                createdAt = now,
                updatedAt = now
            )

        // Mock unit conversion for KG (no conversion needed)
        whenever(unitConverter.toKg(eq(BigDecimal(WEIGHT)), eq(WeightUnit.KG)))
            .thenReturn(BigDecimal(WEIGHT))
        whenever(userDAL.updateUser(eq(USER_ID), eq(NAME), eq(AGE), eq(BigDecimal(HEIGHT)), eq(BigDecimal(WEIGHT))))
            .thenReturn(Mono.just(user))

        val result = userService.updateUser(USER_ID, NAME, AGE, BigDecimal(HEIGHT), BigDecimal(WEIGHT), "KG")

        StepVerifier.create(result)
            .expectNext(user)
            .verifyComplete()

        verify(userDAL).updateUser(USER_ID, NAME, AGE, BigDecimal(HEIGHT), BigDecimal(WEIGHT))
    }

    @Test
    fun `updateUser should convert weight from LBS to KG`() {
        val now = Instant.now()
        val weightInLbs = BigDecimal("150.0")
        val weightInKg = BigDecimal("68.04")
        val user =
            mockUser(
                id = USER_ID,
                name = NAME,
                age = AGE,
                height = BigDecimal(HEIGHT),
                weight = weightInKg,
                createdAt = now,
                updatedAt = now
            )

        whenever(unitConverter.toKg(eq(weightInLbs), eq(WeightUnit.LBS)))
            .thenReturn(weightInKg)
        whenever(userDAL.updateUser(eq(USER_ID), eq(NAME), eq(AGE), eq(BigDecimal(HEIGHT)), eq(weightInKg)))
            .thenReturn(Mono.just(user))

        val result = userService.updateUser(USER_ID, NAME, AGE, BigDecimal(HEIGHT), weightInLbs, "LBS")

        StepVerifier.create(result)
            .expectNext(user)
            .verifyComplete()

        verify(unitConverter).toKg(weightInLbs, WeightUnit.LBS)
        verify(userDAL).updateUser(USER_ID, NAME, AGE, BigDecimal(HEIGHT), weightInKg)
    }

    @Test
    fun `updateUser should throw ValidationException when validation fails`() {
        val invalidWeight = BigDecimal("-10.0")
        val convertedWeight = BigDecimal("-10.0") // Invalid weight after conversion

        whenever(unitConverter.toKg(eq(invalidWeight), eq(WeightUnit.KG)))
            .thenReturn(convertedWeight)

        val result = userService.updateUser(USER_ID, NAME, AGE, BigDecimal(HEIGHT), invalidWeight, "KG")

        StepVerifier.create(result)
            .expectErrorSatisfies { ex ->
                assert(ex is ValidationException)
                assertEquals("User weight must be between 0.01 and 1000 kg, got: -10.0", ex.message)
            }
            .verify()
    }

    @Test
    fun `deleteUser should delete user successfully`() {
        val now = Instant.now()
        val user =
            mockUser(
                id = USER_ID,
                name = NAME,
                age = AGE,
                height = BigDecimal(HEIGHT),
                weight = BigDecimal(WEIGHT),
                createdAt = now,
                updatedAt = now,
                keycloakUserId = null
            )

        whenever(userDAL.selectUserById(USER_ID)).thenReturn(Mono.just(user))
        whenever(userDAL.deleteUser(USER_ID)).thenReturn(Mono.just(user))

        val result = userService.deleteUser(USER_ID)

        StepVerifier.create(result)
            .expectNext(user)
            .verifyComplete()

        verify(userDAL).selectUserById(USER_ID)
        verify(userDAL).deleteUser(USER_ID)
    }

    @Test
    fun `deleteUser should propagate error when user not found`() {
        val error = DatabaseException("User not found")
        whenever(userDAL.selectUserById(USER_ID)).thenReturn(Mono.error(error))

        val result = userService.deleteUser(USER_ID)

        StepVerifier.create(result)
            .expectErrorSatisfies { ex ->
                assert(ex is DatabaseException)
                assertEquals("User not found", ex.message)
            }
            .verify()

        verify(userDAL).selectUserById(USER_ID)
    }
}

package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.TestProtocol
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TestProtocolConfigDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var testProtocolConfigDAL: TestProtocolConfigDAL

    private val testProtocol =
        TestProtocol(
            testName = "vertical_jump",
            displayName = "Vertical Jump",
            description = "Test vertical jump height",
            unit = "cm",
            iconName = "jump",
            isRequired = true,
            displayOrder = 1,
            radarChartColor = "#FF0000",
            radarChartEnabled = true
        )

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        testProtocolConfigDAL = TestProtocolConfigDAL(postgresClient)
    }

    @Test
    fun `getAllTestProtocols should return all test protocols`() {
        val testProtocols =
            listOf(
                testProtocol,
                TestProtocol(
                    testName = "hr_recovery",
                    displayName = "HR Recovery",
                    description = "Test heart rate recovery",
                    unit = "bpm",
                    iconName = "heart",
                    isRequired = true,
                    displayOrder = 2,
                    radarChartColor = "#00FF00",
                    radarChartEnabled = true
                )
            )

        whenever(postgresClient.select<TestProtocol>(any<String>(), any<kotlin.reflect.KClass<TestProtocol>>(), any<Array<Any?>>()))
            .thenReturn(Mono.just(testProtocols))

        val result = testProtocolConfigDAL.getAllTestProtocols()

        StepVerifier.create(result)
            .expectNext(testProtocols)
            .verifyComplete()

        verify(postgresClient).select<TestProtocol>(any<String>(), any<kotlin.reflect.KClass<TestProtocol>>(), any<Array<Any?>>())
    }

    @Test
    fun `getAllTestProtocols should return empty list when no protocols found`() {
        whenever(postgresClient.select<TestProtocol>(any<String>(), any<kotlin.reflect.KClass<TestProtocol>>(), any<Array<Any?>>()))
            .thenReturn(Mono.just(emptyList()))

        val result = testProtocolConfigDAL.getAllTestProtocols()

        StepVerifier.create(result)
            .expectNext(emptyList())
            .verifyComplete()

        verify(postgresClient).select<TestProtocol>(any<String>(), any<kotlin.reflect.KClass<TestProtocol>>(), any<Array<Any?>>())
    }

    @Test
    fun `getTestProtocol should return specific test protocol`() {
        whenever(
            postgresClient.selectIndividual<TestProtocol>(any<String>(), any<kotlin.reflect.KClass<TestProtocol>>(), any<Array<Any?>>())
        )
            .thenReturn(Mono.just(testProtocol))

        val result = testProtocolConfigDAL.getTestProtocol("vertical_jump")

        StepVerifier.create(result)
            .expectNext(testProtocol)
            .verifyComplete()

        verify(
            postgresClient
        ).selectIndividual<TestProtocol>(any<String>(), any<kotlin.reflect.KClass<TestProtocol>>(), eq("vertical_jump"))
    }

    @Test
    fun `getTestProtocol should throw NoResultsFoundException when protocol not found`() {
        whenever(
            postgresClient.selectIndividual<TestProtocol>(any<String>(), any<kotlin.reflect.KClass<TestProtocol>>(), any<Array<Any?>>())
        )
            .thenReturn(Mono.error(NoResultsFoundException("Test protocol not found")))

        val result = testProtocolConfigDAL.getTestProtocol("nonexistent_test")

        StepVerifier.create(result)
            .expectError(NoResultsFoundException::class.java)
            .verify()

        verify(
            postgresClient
        ).selectIndividual<TestProtocol>(any<String>(), any<kotlin.reflect.KClass<TestProtocol>>(), eq("nonexistent_test"))
    }

    @Test
    fun `getAllTestProtocols should handle database error`() {
        val error = RuntimeException("Database connection failed")
        whenever(postgresClient.select<TestProtocol>(any<String>(), any<kotlin.reflect.KClass<TestProtocol>>(), any<Array<Any?>>()))
            .thenReturn(Mono.error(error))

        val result = testProtocolConfigDAL.getAllTestProtocols()

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()

        verify(postgresClient).select<TestProtocol>(any<String>(), any<kotlin.reflect.KClass<TestProtocol>>(), any<Array<Any?>>())
    }

    @Test
    fun `getTestProtocol should handle database error`() {
        val error = RuntimeException("Database connection failed")
        whenever(
            postgresClient.selectIndividual<TestProtocol>(any<String>(), any<kotlin.reflect.KClass<TestProtocol>>(), any<Array<Any?>>())
        )
            .thenReturn(Mono.error(error))

        val result = testProtocolConfigDAL.getTestProtocol("vertical_jump")

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()

        verify(
            postgresClient
        ).selectIndividual<TestProtocol>(any<String>(), any<kotlin.reflect.KClass<TestProtocol>>(), eq("vertical_jump"))
    }
}

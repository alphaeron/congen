package com.congen.util

import com.fasterxml.jackson.databind.ObjectMapper
import net.rubyeye.xmemcached.MemcachedClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.test.StepVerifier
import java.time.Duration
import java.util.Base64

@ExtendWith(MockitoExtension::class)
class ReactiveMemcachedCacheTest {
    @Mock
    private lateinit var memcachedClient: MemcachedClient

    private lateinit var reactiveCache: ReactiveMemcachedCache
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        objectMapper = ObjectMapper()
        reactiveCache = ReactiveMemcachedCache(memcachedClient, objectMapper)
    }

    @Test
    fun `set should store value in cache`() {
        val key = "test:key"
        val value = TestData("test", 123)
        val ttl = Duration.ofMinutes(30)
        val encodedKey = Base64.getEncoder().encodeToString("congen:$key".toByteArray())

        whenever(memcachedClient.set(eq(encodedKey), eq(1800), any()))
            .thenReturn(true)

        StepVerifier.create(reactiveCache.set(key, value, ttl))
            .expectNext(true)
            .verifyComplete()

        verify(memcachedClient).set(eq(encodedKey), eq(1800), any())
    }

    @Test
    fun `get should retrieve value from cache`() {
        val key = "test:key"
        val expectedValue = "test-string"
        val jsonValue = "\"$expectedValue\""
        val encodedKey = Base64.getEncoder().encodeToString("congen:$key".toByteArray())

        whenever(memcachedClient.get<String>(encodedKey))
            .thenReturn(jsonValue)

        StepVerifier.create(reactiveCache.get<String>(key))
            .expectNext(expectedValue)
            .verifyComplete()
    }

    @Test
    fun `get should throw CacheMissException when key not found`() {
        val key = "test:key"
        val encodedKey = Base64.getEncoder().encodeToString("congen:$key".toByteArray())

        whenever(memcachedClient.get<String>(encodedKey))
            .thenReturn(null)

        StepVerifier.create(reactiveCache.get<TestData>(key))
            .expectError(CacheMissException::class.java)
            .verify()
    }

    @Test
    fun `delete should remove value from cache`() {
        val key = "test:key"
        val encodedKey = Base64.getEncoder().encodeToString("congen:$key".toByteArray())

        whenever(memcachedClient.delete(encodedKey))
            .thenReturn(true)

        StepVerifier.create(reactiveCache.delete(key))
            .expectNext(true)
            .verifyComplete()

        verify(memcachedClient).delete(encodedKey)
    }

    @Test
    fun `increment should increment numeric value`() {
        val key = "test:key"
        val delta = 5L
        val expectedValue = 10L
        val encodedKey = Base64.getEncoder().encodeToString("congen:$key".toByteArray())

        whenever(memcachedClient.incr(encodedKey, delta))
            .thenReturn(expectedValue)

        StepVerifier.create(reactiveCache.increment(key, delta))
            .expectNext(expectedValue)
            .verifyComplete()

        verify(memcachedClient).incr(encodedKey, delta)
    }

    data class TestData(
        val name: String,
        val value: Int
    )
}

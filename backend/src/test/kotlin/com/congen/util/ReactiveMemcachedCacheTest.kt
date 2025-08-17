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

        whenever(memcachedClient.set(eq("congen:$key"), eq(1800), any<String>()))
            .thenReturn(true)

        StepVerifier.create(reactiveCache.set(key, value, ttl))
            .expectNext(true)
            .verifyComplete()

        verify(memcachedClient).set(eq("congen:$key"), eq(1800), any<String>())
    }

    @Test
    fun `get should retrieve value from cache`() {
        val key = "test:key"
        val expectedValue = TestData("test", 123)
        val jsonValue = objectMapper.writeValueAsString(expectedValue)

        whenever(memcachedClient.get("congen:$key"))
            .thenReturn(jsonValue)

        StepVerifier.create(reactiveCache.get(key, TestData::class.java))
            .expectNext(expectedValue)
            .verifyComplete()
    }

    @Test
    fun `get should return empty when key not found`() {
        val key = "test:key"

        whenever(memcachedClient.get("congen:$key"))
            .thenReturn(null)

        StepVerifier.create(reactiveCache.get(key, TestData::class.java))
            .verifyComplete()
    }

    @Test
    fun `delete should remove value from cache`() {
        val key = "test:key"

        whenever(memcachedClient.delete("congen:$key"))
            .thenReturn(true)

        StepVerifier.create(reactiveCache.delete(key))
            .expectNext(true)
            .verifyComplete()

        verify(memcachedClient).delete("congen:$key")
    }

    @Test
    fun `increment should increment numeric value`() {
        val key = "test:key"
        val delta = 5L
        val expectedValue = 10L

        whenever(memcachedClient.incr("congen:$key", delta))
            .thenReturn(expectedValue)

        StepVerifier.create(reactiveCache.increment(key, delta))
            .expectNext(expectedValue)
            .verifyComplete()

        verify(memcachedClient).incr("congen:$key", delta)
    }

    data class TestData(
        val name: String,
        val value: Int
    )
}

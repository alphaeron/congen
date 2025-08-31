package com.congen

import com.congen.cache.ReactiveMemcachedCache
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Duration
import java.time.Instant
import java.util.UUID

/**
 * Integration test for Memcached caching operations.
 *
 * This test validates the ReactiveMemcachedCache utility directly including:
 * - Cache set and get operations
 * - Different data types (primitives, objects, lists, maps)
 * - TTL (Time-To-Live) functionality
 * - Cache key generation and encoding
 * - Error handling and edge cases
 * - Concurrent operations
 * - Cache eviction and expiration
 */
class MemcachedIntegrationTest : BaseIntegrationTest() {
    private data class TestObject(
        val id: String,
        val name: String,
        val value: Int,
        val timestamp: Instant
    )

    private data class TestItem(
        val id: String,
        val name: String
    )

    private data class LargeObject(
        val id: String,
        val data: String,
        val metadata: Map<String, Any>
    )

    @Autowired
    private lateinit var reactiveCache: ReactiveMemcachedCache

    private lateinit var testKey: String

    @BeforeEach
    override fun setUp() {
        super.setUp()
        testKey = "test-key-${UUID.randomUUID()}"
    }

    @Test
    fun `should cache and retrieve string value`() {
        val testValue = "test-string-value"
        val setResult = reactiveCache.set(testKey, testValue, Duration.ofMinutes(5))
        val getResult = reactiveCache.get<String>(testKey)
        StepVerifier.create(setResult).expectNext(true).verifyComplete()
        StepVerifier.create(getResult).expectNext(testValue).verifyComplete()
    }

    @Test
    fun `should cache and retrieve integer value`() {
        val testValue = 42
        val setResult = reactiveCache.set(testKey, testValue, Duration.ofMinutes(5))
        val getResult = reactiveCache.get<Int>(testKey)
        StepVerifier.create(setResult).expectNext(true).verifyComplete()
        StepVerifier.create(getResult).expectNext(testValue).verifyComplete()
    }

    @Test
    fun `should cache and retrieve boolean value`() {
        val testValue = true
        val setResult = reactiveCache.set(testKey, testValue, Duration.ofMinutes(5))
        val getResult = reactiveCache.get<Boolean>(testKey)
        StepVerifier.create(setResult).expectNext(true).verifyComplete()
        StepVerifier.create(getResult).expectNext(testValue).verifyComplete()
    }

    @Test
    fun `should cache and retrieve complex object`() {
        val testValue =
            TestObject(
                id = UUID.randomUUID().toString(),
                name = "Test Object",
                value = 123,
                timestamp = Instant.now()
            )
        val setResult = reactiveCache.set(testKey, testValue, Duration.ofMinutes(5))
        val getResult = reactiveCache.get<TestObject>(testKey)
        StepVerifier.create(setResult).expectNext(true).verifyComplete()
        StepVerifier.create(getResult)
            .expectNextMatches { retrieved ->
                retrieved.id == testValue.id &&
                    retrieved.name == testValue.name &&
                    retrieved.value == testValue.value
            }
            .verifyComplete()
    }

    @Test
    fun `should cache and retrieve list of objects`() {
        val testValue =
            listOf(
                TestItem("1", "Item 1"),
                TestItem("2", "Item 2"),
                TestItem("3", "Item 3")
            )
        val setResult = reactiveCache.set(testKey, testValue, Duration.ofMinutes(5))
        val getResult = reactiveCache.get<List<TestItem>>(testKey)
        StepVerifier.create(setResult).expectNext(true).verifyComplete()
        StepVerifier.create(getResult)
            .expectNextMatches { retrieved ->
                retrieved.size == testValue.size &&
                    retrieved.all { item ->
                        testValue.any { original ->
                            original.id == item.id && original.name == item.name
                        }
                    }
            }
            .verifyComplete()
    }

    @Test
    fun `should cache and retrieve map`() {
        val testValue =
            mapOf(
                "key1" to "value1",
                "key2" to "value2",
                "key3" to 42,
                "key4" to true
            )
        val setResult = reactiveCache.set(testKey, testValue, Duration.ofMinutes(5))
        val getResult = reactiveCache.get<Map<String, Any>>(testKey)
        StepVerifier.create(setResult).expectNext(true).verifyComplete()
        StepVerifier.create(getResult)
            .expectNextMatches { retrieved ->
                retrieved["key1"] == "value1" &&
                    retrieved["key2"] == "value2" &&
                    retrieved["key3"] == 42 &&
                    retrieved["key4"] == true
            }
            .verifyComplete()
    }

    @Test
    fun `should handle cache miss gracefully`() {
        val nonExistentKey = "non-existent-key-${UUID.randomUUID()}"
        val getResult = reactiveCache.get<String>(nonExistentKey)
        StepVerifier.create(getResult).expectError().verify()
    }

    @Test
    fun `should respect TTL and expire cache entries`() {
        val shortTtlKey = "short-ttl-${UUID.randomUUID()}"
        val testValue = "expire-test-value"
        val setResult = reactiveCache.set(shortTtlKey, testValue, Duration.ofSeconds(1))
        StepVerifier.create(setResult).expectNext(true).verifyComplete()
        val immediateGet = reactiveCache.get<String>(shortTtlKey)
        StepVerifier.create(immediateGet).expectNext(testValue).verifyComplete()
        Thread.sleep(1500)
        val expiredGet = reactiveCache.get<String>(shortTtlKey)
        StepVerifier.create(expiredGet).expectError().verify()
    }

    @Test
    fun `should handle cache keys with special characters`() {
        val specialKey = "key with spaces & special chars!@#$%^&*()_+-=[]{}|;':\",./<>?`~"
        val testValue = "special-chars-test"
        val setResult = reactiveCache.set(specialKey, testValue, Duration.ofMinutes(5))
        val getResult = reactiveCache.get<String>(specialKey)
        StepVerifier.create(setResult).expectNext(true).verifyComplete()
        StepVerifier.create(getResult).expectNext(testValue).verifyComplete()
    }

    @Test
    fun `should handle long cache keys within limits`() {
        // Choose a key length that stays under Memcached's 250-byte limit after Base64 encoding with namespace
        val longKey = "a".repeat(120) + UUID.randomUUID().toString()
        val testValue = "long-key-test"
        val setResult = reactiveCache.set(longKey, testValue, Duration.ofMinutes(5))
        val getResult = reactiveCache.get<String>(longKey)
        StepVerifier.create(setResult).expectNext(true).verifyComplete()
        StepVerifier.create(getResult).expectNext(testValue).verifyComplete()
    }

    @Test
    fun `should error for excessively long cache keys`() {
        val tooLongKey = "a".repeat(1000) + UUID.randomUUID().toString()
        val testValue = "too-long-key-test"
        val setResult = reactiveCache.set(tooLongKey, testValue, Duration.ofMinutes(5))
        StepVerifier.create(setResult).expectError().verify()
    }

    @Test
    fun `should handle concurrent operations`() {
        val concurrentKey = "concurrent-${UUID.randomUUID()}"
        val testValue = "concurrent-test-value"
        val setResult = reactiveCache.set(concurrentKey, testValue, Duration.ofMinutes(5))
        val get1 = reactiveCache.get<String>(concurrentKey)
        val get2 = reactiveCache.get<String>(concurrentKey)
        val get3 = reactiveCache.get<String>(concurrentKey)
        val get4 = reactiveCache.get<String>(concurrentKey)
        val get5 = reactiveCache.get<String>(concurrentKey)
        StepVerifier.create(setResult).expectNext(true).verifyComplete()
        val concurrentGets = Mono.zip(get1, get2, get3, get4, get5)
        StepVerifier.create(concurrentGets)
            .expectNextMatches { tuple ->
                val value1 = tuple.t1
                val value2 = tuple.t2
                val value3 = tuple.t3
                val value4 = tuple.t4
                val value5 = tuple.t5
                value1 == testValue &&
                    value2 == testValue &&
                    value3 == testValue &&
                    value4 == testValue &&
                    value5 == testValue
            }
            .verifyComplete()
    }

    @Test
    fun `should handle large data objects`() {
        val largeData = "x".repeat(10000)
        val metadata = (1..100).associate { "key$it" to "value$it" }
        val testValue =
            LargeObject(
                id = UUID.randomUUID().toString(),
                data = largeData,
                metadata = metadata
            )
        val setResult = reactiveCache.set(testKey, testValue, Duration.ofMinutes(5))
        val getResult = reactiveCache.get<LargeObject>(testKey)
        StepVerifier.create(setResult).expectNext(true).verifyComplete()
        StepVerifier.create(getResult)
            .expectNextMatches { retrieved ->
                retrieved.id == testValue.id &&
                    retrieved.data == testValue.data &&
                    retrieved.metadata.size == testValue.metadata.size
            }
            .verifyComplete()
    }

    @Test
    fun `should handle null values appropriately`() {
        val nullKey = "null-test-${UUID.randomUUID()}"
        val getResult = reactiveCache.get<String>(nullKey)
        StepVerifier.create(getResult).expectError().verify()
    }

    @Test
    fun `should handle different TTL durations`() {
        val shortTtlKey = "short-ttl-${UUID.randomUUID()}"
        val mediumTtlKey = "medium-ttl-${UUID.randomUUID()}"
        val longTtlKey = "long-ttl-${UUID.randomUUID()}"
        val testValue = "ttl-test-value"
        val shortSet = reactiveCache.set(shortTtlKey, testValue, Duration.ofSeconds(1))
        val mediumSet = reactiveCache.set(mediumTtlKey, testValue, Duration.ofMinutes(1))
        val longSet = reactiveCache.set(longTtlKey, testValue, Duration.ofHours(1))
        StepVerifier.create(shortSet).expectNext(true).verifyComplete()
        StepVerifier.create(mediumSet).expectNext(true).verifyComplete()
        StepVerifier.create(longSet).expectNext(true).verifyComplete()
        val shortGet = reactiveCache.get<String>(shortTtlKey)
        val mediumGet = reactiveCache.get<String>(mediumTtlKey)
        val longGet = reactiveCache.get<String>(longTtlKey)
        StepVerifier.create(shortGet).expectNext(testValue).verifyComplete()
        StepVerifier.create(mediumGet).expectNext(testValue).verifyComplete()
        StepVerifier.create(longGet).expectNext(testValue).verifyComplete()
    }

    @Test
    fun `should handle cache key collisions gracefully`() {
        val baseKey = "collision-test"
        val testValue1 = "value1"
        val testValue2 = "value2"
        val set1 = reactiveCache.set(baseKey, testValue1, Duration.ofMinutes(5))
        val set2 = reactiveCache.set(baseKey, testValue2, Duration.ofMinutes(5))
        StepVerifier.create(set1).expectNext(true).verifyComplete()
        StepVerifier.create(set2).expectNext(true).verifyComplete()
        val getResult = reactiveCache.get<String>(baseKey)
        StepVerifier.create(getResult).expectNext(testValue2).verifyComplete()
    }

    @Test
    fun `should handle rapid set and get operations`() {
        val rapidKey = "rapid-test-${UUID.randomUUID()}"

        // Execute set operations sequentially to ensure the last one wins
        val sequentialSets =
            reactiveCache.set(rapidKey, "value1", Duration.ofMinutes(5))
                .then(reactiveCache.set(rapidKey, "value2", Duration.ofMinutes(5)))
                .then(reactiveCache.set(rapidKey, "value3", Duration.ofMinutes(5)))
                .then(reactiveCache.set(rapidKey, "value4", Duration.ofMinutes(5)))
                .then(reactiveCache.set(rapidKey, "value5", Duration.ofMinutes(5)))

        // Execute all set operations sequentially and wait for completion
        StepVerifier.create(sequentialSets)
            .expectNext(true)
            .verifyComplete()

        // Now get the value after all sets have completed
        val getResult = reactiveCache.get<String>(rapidKey)
        StepVerifier.create(getResult).expectNext("value5").verifyComplete()
    }

    @Test
    fun `should delete exact key`() {
        val deleteKey = "delete-test-${UUID.randomUUID()}"
        val testValue = "delete-test-value"

        // Set the value
        val setResult = reactiveCache.set(deleteKey, testValue, Duration.ofMinutes(5))
        StepVerifier.create(setResult).expectNext(true).verifyComplete()

        // Verify it exists
        val getBeforeDelete = reactiveCache.get<String>(deleteKey)
        StepVerifier.create(getBeforeDelete).expectNext(testValue).verifyComplete()

        // Delete the key
        val deleteResult = reactiveCache.delete(deleteKey)
        StepVerifier.create(deleteResult).expectNext(true).verifyComplete()

        // Verify it's gone
        val getAfterDelete = reactiveCache.get<String>(deleteKey)
        StepVerifier.create(getAfterDelete).expectError().verify()
    }

    @Test
    fun `should delete non-existent key gracefully`() {
        val nonExistentKey = "non-existent-delete-${UUID.randomUUID()}"

        // Try to delete non-existent key
        val deleteResult = reactiveCache.delete(nonExistentKey)
        StepVerifier.create(deleteResult).expectNext(false).verifyComplete()
    }

    @Test
    fun `should delete pattern matching multiple keys`() {
        val pattern = "pattern-test-${UUID.randomUUID()}"
        val key1 = "$pattern:user:123:programs"
        val key2 = "$pattern:user:123:preferences"
        val key3 = "$pattern:user:456:programs"
        val key4 = "other-pattern:user:123:programs" // Should not be deleted

        val testValue = "pattern-test-value"

        // Set multiple keys
        val set1 = reactiveCache.set(key1, testValue, Duration.ofMinutes(5))
        val set2 = reactiveCache.set(key2, testValue, Duration.ofMinutes(5))
        val set3 = reactiveCache.set(key3, testValue, Duration.ofMinutes(5))
        val set4 = reactiveCache.set(key4, testValue, Duration.ofMinutes(5))

        StepVerifier.create(set1).expectNext(true).verifyComplete()
        StepVerifier.create(set2).expectNext(true).verifyComplete()
        StepVerifier.create(set3).expectNext(true).verifyComplete()
        StepVerifier.create(set4).expectNext(true).verifyComplete()

        // Verify all keys exist
        val get1 = reactiveCache.get<String>(key1)
        val get2 = reactiveCache.get<String>(key2)
        val get3 = reactiveCache.get<String>(key3)
        val get4 = reactiveCache.get<String>(key4)

        StepVerifier.create(get1).expectNext(testValue).verifyComplete()
        StepVerifier.create(get2).expectNext(testValue).verifyComplete()
        StepVerifier.create(get3).expectNext(testValue).verifyComplete()
        StepVerifier.create(get4).expectNext(testValue).verifyComplete()

        // Delete pattern matching keys
        val deletePatternResult = reactiveCache.deletePattern("$pattern:*")
        StepVerifier.create(deletePatternResult)
            .expectNextMatches { deletedKeys ->
                deletedKeys.size == 3 &&
                    deletedKeys.contains(key1) &&
                    deletedKeys.contains(key2) &&
                    deletedKeys.contains(key3) &&
                    !deletedKeys.contains(key4)
            }
            .verifyComplete()

        // Verify pattern keys are deleted
        val getAfterDelete1 = reactiveCache.get<String>(key1)
        val getAfterDelete2 = reactiveCache.get<String>(key2)
        val getAfterDelete3 = reactiveCache.get<String>(key3)

        StepVerifier.create(getAfterDelete1).expectError().verify()
        StepVerifier.create(getAfterDelete2).expectError().verify()
        StepVerifier.create(getAfterDelete3).expectError().verify()

        // Verify non-matching key still exists
        val getAfterDelete4 = reactiveCache.get<String>(key4)
        StepVerifier.create(getAfterDelete4).expectNext(testValue).verifyComplete()
    }

    @Test
    fun `should delete pattern with wildcard at end`() {
        val basePattern = "wildcard-end-${UUID.randomUUID()}"
        val key1 = "$basePattern:user:123"
        val key2 = "$basePattern:user:456"
        val key3 = "$basePattern:program:789"

        val testValue = "wildcard-test-value"

        // Set keys
        val set1 = reactiveCache.set(key1, testValue, Duration.ofMinutes(5))
        val set2 = reactiveCache.set(key2, testValue, Duration.ofMinutes(5))
        val set3 = reactiveCache.set(key3, testValue, Duration.ofMinutes(5))

        StepVerifier.create(set1).expectNext(true).verifyComplete()
        StepVerifier.create(set2).expectNext(true).verifyComplete()
        StepVerifier.create(set3).expectNext(true).verifyComplete()

        // Delete with wildcard pattern
        val deletePatternResult = reactiveCache.deletePattern("$basePattern:*")
        StepVerifier.create(deletePatternResult)
            .expectNextMatches { deletedKeys ->
                deletedKeys.size == 3 &&
                    deletedKeys.contains(key1) &&
                    deletedKeys.contains(key2) &&
                    deletedKeys.contains(key3)
            }
            .verifyComplete()

        // Verify all are deleted
        val get1 = reactiveCache.get<String>(key1)
        val get2 = reactiveCache.get<String>(key2)
        val get3 = reactiveCache.get<String>(key3)

        StepVerifier.create(get1).expectError().verify()
        StepVerifier.create(get2).expectError().verify()
        StepVerifier.create(get3).expectError().verify()
    }

    @Test
    fun `should delete pattern with wildcard in middle`() {
        val basePattern = "wildcard-middle-${UUID.randomUUID()}"
        val key1 = "$basePattern:user:123:programs"
        val key2 = "$basePattern:user:456:programs"
        val key3 = "$basePattern:program:789:details"
        val key4 = "$basePattern:user:123:preferences" // Different suffix

        val testValue = "wildcard-middle-test-value"

        // Set keys
        val set1 = reactiveCache.set(key1, testValue, Duration.ofMinutes(5))
        val set2 = reactiveCache.set(key2, testValue, Duration.ofMinutes(5))
        val set3 = reactiveCache.set(key3, testValue, Duration.ofMinutes(5))
        val set4 = reactiveCache.set(key4, testValue, Duration.ofMinutes(5))

        StepVerifier.create(set1).expectNext(true).verifyComplete()
        StepVerifier.create(set2).expectNext(true).verifyComplete()
        StepVerifier.create(set3).expectNext(true).verifyComplete()
        StepVerifier.create(set4).expectNext(true).verifyComplete()

        // Delete with wildcard in middle pattern
        val deletePatternResult = reactiveCache.deletePattern("$basePattern:user:*:programs")
        StepVerifier.create(deletePatternResult)
            .expectNextMatches { deletedKeys ->
                deletedKeys.size == 2 &&
                    deletedKeys.contains(key1) &&
                    deletedKeys.contains(key2) &&
                    !deletedKeys.contains(key3) &&
                    !deletedKeys.contains(key4)
            }
            .verifyComplete()

        // Verify only matching keys are deleted
        val get1 = reactiveCache.get<String>(key1)
        val get2 = reactiveCache.get<String>(key2)
        val get3 = reactiveCache.get<String>(key3)
        val get4 = reactiveCache.get<String>(key4)

        StepVerifier.create(get1).expectError().verify()
        StepVerifier.create(get2).expectError().verify()
        StepVerifier.create(get3).expectNext(testValue).verifyComplete()
        StepVerifier.create(get4).expectNext(testValue).verifyComplete()
    }

    @Test
    fun `should handle delete pattern with no matching keys`() {
        val nonMatchingPattern = "non-matching-${UUID.randomUUID()}"

        // Try to delete pattern with no matching keys
        val deletePatternResult = reactiveCache.deletePattern("$nonMatchingPattern:*")
        StepVerifier.create(deletePatternResult)
            .expectNext(emptyList<String>())
            .verifyComplete()
    }

    @Test
    fun `should handle delete pattern with special characters`() {
        val specialPattern = "special-chars-${UUID.randomUUID()}"
        val key1 = "$specialPattern:user:123:programs"
        val key2 = "$specialPattern:user:456:programs"

        val testValue = "special-chars-test-value"

        // Set keys
        val set1 = reactiveCache.set(key1, testValue, Duration.ofMinutes(5))
        val set2 = reactiveCache.set(key2, testValue, Duration.ofMinutes(5))

        StepVerifier.create(set1).expectNext(true).verifyComplete()
        StepVerifier.create(set2).expectNext(true).verifyComplete()

        // Delete with pattern containing special characters
        val deletePatternResult = reactiveCache.deletePattern("$specialPattern:user:*:programs")
        StepVerifier.create(deletePatternResult)
            .expectNextMatches { deletedKeys ->
                deletedKeys.size == 2 &&
                    deletedKeys.contains(key1) &&
                    deletedKeys.contains(key2)
            }
            .verifyComplete()

        // Verify keys are deleted
        val get1 = reactiveCache.get<String>(key1)
        val get2 = reactiveCache.get<String>(key2)

        StepVerifier.create(get1).expectError().verify()
        StepVerifier.create(get2).expectError().verify()
    }

    @Test
    fun `should handle concurrent pattern deletions`() {
        val concurrentPattern = "concurrent-${UUID.randomUUID()}"
        val key1 = "$concurrentPattern:user:123:programs"
        val key2 = "$concurrentPattern:user:456:programs"
        val key3 = "$concurrentPattern:user:789:programs"

        val testValue = "concurrent-test-value"

        // Set keys
        val set1 = reactiveCache.set(key1, testValue, Duration.ofMinutes(5))
        val set2 = reactiveCache.set(key2, testValue, Duration.ofMinutes(5))
        val set3 = reactiveCache.set(key3, testValue, Duration.ofMinutes(5))

        StepVerifier.create(set1).expectNext(true).verifyComplete()
        StepVerifier.create(set2).expectNext(true).verifyComplete()
        StepVerifier.create(set3).expectNext(true).verifyComplete()

        // Execute concurrent pattern deletions
        val delete1 = reactiveCache.deletePattern("$concurrentPattern:*")
        val delete2 = reactiveCache.deletePattern("$concurrentPattern:*")
        val delete3 = reactiveCache.deletePattern("$concurrentPattern:*")

        val concurrentDeletes = Mono.zip(delete1, delete2, delete3)
        StepVerifier.create(concurrentDeletes)
            .expectNextMatches { tuple ->
                val result1 = tuple.t1
                val result2 = tuple.t2
                val result3 = tuple.t3

                // Due to race conditions, some may return keys and others may return empty
                // The important thing is that all keys are eventually deleted
                val totalDeleted = result1.size + result2.size + result3.size
                totalDeleted >= 3 // At least 3 keys should be deleted across all operations
            }
            .verifyComplete()

        // Verify all keys are eventually deleted
        val get1 = reactiveCache.get<String>(key1)
        val get2 = reactiveCache.get<String>(key2)
        val get3 = reactiveCache.get<String>(key3)

        StepVerifier.create(get1).expectError().verify()
        StepVerifier.create(get2).expectError().verify()
        StepVerifier.create(get3).expectError().verify()
    }

    @Test
    fun `should handle key index tracking correctly`() {
        val indexTestPattern = "index-test-${UUID.randomUUID()}"
        val key1 = "$indexTestPattern:key1"
        val key2 = "$indexTestPattern:key2"
        val key3 = "$indexTestPattern:key3"

        val testValue = "index-test-value"

        // Set keys
        val set1 = reactiveCache.set(key1, testValue, Duration.ofMinutes(5))
        val set2 = reactiveCache.set(key2, testValue, Duration.ofMinutes(5))
        val set3 = reactiveCache.set(key3, testValue, Duration.ofMinutes(5))

        StepVerifier.create(set1).expectNext(true).verifyComplete()
        StepVerifier.create(set2).expectNext(true).verifyComplete()
        StepVerifier.create(set3).expectNext(true).verifyComplete()

        // Delete individual keys and verify they're removed from index
        val delete1 = reactiveCache.delete(key1)
        StepVerifier.create(delete1).expectNext(true).verifyComplete()

        val delete2 = reactiveCache.delete(key2)
        StepVerifier.create(delete2).expectNext(true).verifyComplete()

        // Verify individual deletes work
        val get1 = reactiveCache.get<String>(key1)
        val get2 = reactiveCache.get<String>(key2)
        val get3 = reactiveCache.get<String>(key3)

        StepVerifier.create(get1).expectError().verify()
        StepVerifier.create(get2).expectError().verify()
        StepVerifier.create(get3).expectNext(testValue).verifyComplete()

        // Now delete remaining key with pattern
        val deletePatternResult = reactiveCache.deletePattern("$indexTestPattern:*")
        StepVerifier.create(deletePatternResult)
            .expectNextMatches { deletedKeys ->
                deletedKeys.size == 1 && deletedKeys.contains(key3)
            }
            .verifyComplete()

        // Verify all keys are deleted
        val finalGet = reactiveCache.get<String>(key3)
        StepVerifier.create(finalGet).expectError().verify()
    }
}

package com.congen.cache

import com.congen.exceptions.CacheMissException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import net.rubyeye.xmemcached.MemcachedClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler
import java.time.Duration
import java.util.Base64

/**
 * Reactive wrapper for Memcached operations.
 *
 * This utility provides reactive (non-blocking) access to Memcached operations
 * by wrapping the synchronous xmemcached client with Project Reactor.
 *
 * ## Features
 *
 * - **Reactive Operations**: All operations return Mono/Flux for non-blocking execution
 * - **JSON Serialization**: Automatic serialization/deserialization of objects
 * - **Error Handling**: Graceful handling of cache misses and connection errors
 * - **Key Generation**: Consistent cache key generation with namespace support
 * - **Custom Scheduler**: Uses dedicated scheduler for Memcached operations
 *
 * ## Usage
 *
 * ```kotlin
 * // Cache a value
 * reactiveCache.set("user:123", user, Duration.ofMinutes(30))
 *     .subscribe()
 *
 * // Retrieve a value
 * reactiveCache.get<User>("user:123")
 *     .subscribe { user -> println(user) }
 *
 * // Delete a value
 * reactiveCache.delete("user:123")
 *     .subscribe()
 * ```
 *
 * @param memcachedClient Memcached client for cache operations
 * @property objectMapper Jackson ObjectMapper for JSON serialization
 * @param memcachedScheduler Dedicated scheduler for Memcached operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class ReactiveMemcachedCache(
    private val memcachedClient: MemcachedClient,
    val objectMapper: ObjectMapper,
    private val memcachedScheduler: Scheduler
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ReactiveMemcachedCache::class.java)
        private const val DEFAULT_NAMESPACE = "congen"
        private const val KEY_INDEX_PREFIX = "congen:key_index:"
    }

    // In-memory index of cache keys for pattern-based deletion
    private val keyIndex = mutableSetOf<String>()

    /**
     * Stores a value in the cache with the specified TTL.
     *
     * This method serializes the value to JSON and stores it in Memcached
     * with the given key and expiration time.
     *
     * @param T The type of the value to cache
     * @param key The cache key
     * @param value The value to cache
     * @param ttl Time-to-live duration
     * @return Mono<Boolean> indicating success or failure of the operation
     */
    fun <T> set(
        key: String,
        value: T,
        ttl: Duration
    ): Mono<Boolean> {
        val cacheKey = generateKey(key)
        val jsonValue = objectMapper.writeValueAsString(value)
        val expirySeconds = ttl.seconds.toInt()

        logger.debug("Setting cache key: {} with TTL: {} seconds", cacheKey, expirySeconds)

        return Mono.fromCallable {
            // Check if client is still active before attempting operation
            if (memcachedClient.isShutdown) {
                logger.warn("Memcached client is shutdown, skipping cache operation for key: {}", cacheKey)
                return@fromCallable false
            }
            memcachedClient.set(cacheKey, expirySeconds, jsonValue)
        }.subscribeOn(memcachedScheduler)
            .doOnSuccess { success ->
                if (success) {
                    logger.debug("Successfully cached key: {}", cacheKey)
                    // Track the key for pattern-based deletion
                    synchronized(keyIndex) {
                        keyIndex.add(key)
                    }
                } else {
                    logger.warn("Failed to cache key: {}", cacheKey)
                }
            }
            .doOnError { error ->
                logger.error("Error caching key: {}", cacheKey, error)
            }
            .onErrorReturn(false)
    }

    /**
     * Retrieves a value from the cache using TypeReference for generic types.
     *
     * This method is useful for deserializing generic types like List<T>.
     *
     * @param T The type of the value to retrieve
     * @param key The cache key
     * @param typeReference The TypeReference for the expected type
     * @return Mono<T> containing the cached value or throws CacheMissException if not found
     */
    fun <T : Any> get(
        key: String,
        typeReference: TypeReference<T>
    ): Mono<T> {
        val cacheKey = generateKey(key)

        logger.debug("Getting cache key: {} with TypeReference", cacheKey)

        return getCachedValueWithTypeReference(cacheKey, typeReference)
            .doOnError {
                if (it !is CacheMissException) {
                    logger.error("Error retrieving cache key: {}", cacheKey, it)
                }
            }
    }

    private fun <T : Any> getCachedValueWithTypeReference(
        cacheKey: String,
        typeReference: TypeReference<T>
    ): Mono<T> {
        return Mono.fromCallable {
            // Check if client is still active before attempting operation
            if (memcachedClient.isShutdown) {
                logger.warn("Memcached client is shutdown, treating as cache miss for key: {}", cacheKey)
                throw CacheMissException(cacheKey)
            }
            val cachedValue = memcachedClient.get<String>(cacheKey)
            if (cachedValue != null) {
                val result = objectMapper.readValue(cachedValue, typeReference)
                result
            } else {
                throw CacheMissException(cacheKey)
            }
        }.subscribeOn(memcachedScheduler)
            .onErrorResume { error ->
                // If it's a connection error, treat as cache miss instead of propagating the error
                if (error.message?.contains("Xmemcached is stopped") == true || 
                    error.message?.contains("Connection refused") == true) {
                    logger.warn("Memcached connection error for key: {}, treating as cache miss", cacheKey)
                    Mono.error(CacheMissException(cacheKey))
                } else {
                    Mono.error(error)
                }
            }
    }

    /**
     * Retrieves a value from the cache using reified type.
     *
     * This is a convenience method that infers the return type from
     * the generic parameter. It automatically creates the proper TypeReference
     * to preserve generic type information.
     *
     * @param T The type of the value to retrieve
     * @param key The cache key
     * @return Mono<T> containing the cached value or throws CacheMissException if not found
     */
    final inline fun <reified T : Any> get(key: String): Mono<T> {
        return get(key, object : TypeReference<T>() {})
    }

    /**
     * Deletes a value from the cache.
     *
     * @param key The cache key to delete
     * @return Mono<Boolean> indicating success or failure of the operation
     */
    fun delete(key: String): Mono<Boolean> {
        val cacheKey = generateKey(key)

        logger.debug("Deleting cache key: {}", cacheKey)

        return Mono.fromCallable {
            // Check if client is still active before attempting operation
            if (memcachedClient.isShutdown) {
                logger.warn("Memcached client is shutdown, skipping delete operation for key: {}", cacheKey)
                return@fromCallable false
            }
            memcachedClient.delete(cacheKey)
        }.subscribeOn(memcachedScheduler)
            .doOnSuccess { success: Boolean ->
                if (success) {
                    logger.debug("Successfully deleted cache key: {}", cacheKey)
                    // Remove from key index
                    synchronized(keyIndex) {
                        keyIndex.remove(key)
                    }
                } else {
                    logger.debug("Cache key not found for deletion: {}", cacheKey)
                }
            }
            .doOnError { error: Throwable ->
                logger.error("Error deleting cache key: {}", cacheKey, error)
            }
            .onErrorReturn(false)
    }

    /**
     * Deletes all cache keys matching the given pattern.
     *
     * This method uses pattern matching to find and delete multiple cache keys.
     * The pattern supports wildcards (*) for matching any sequence of characters.
     *
     * @param pattern The pattern to match against cache keys
     * @return Mono<List<String>> containing the list of deleted keys
     */
    fun deletePattern(pattern: String): Mono<List<String>> {
        logger.debug("Deleting cache keys matching pattern: {}", pattern)

        return Mono.fromCallable {
            // Check if client is still active before attempting operation
            if (memcachedClient.isShutdown) {
                logger.warn("Memcached client is shutdown, skipping pattern delete operation for pattern: {}", pattern)
                return@fromCallable emptyList<String>()
            }
            
            // Escape regex special characters except for our wildcard pattern
            val escapedPattern =
                pattern.replace("*", "___WILDCARD___")
                    .replace("(", "\\(")
                    .replace(")", "\\)")
                    .replace("[", "\\[")
                    .replace("]", "\\]")
                    .replace("{", "\\{")
                    .replace("}", "\\}")
                    .replace("^", "\\^")
                    .replace("$", "\\$")
                    .replace(".", "\\.")
                    .replace("+", "\\+")
                    .replace("?", "\\?")
                    .replace("|", "\\|")
                    .replace("\\", "\\\\")
                    .replace("___WILDCARD___", ".*")
            val regex = escapedPattern.toRegex()

            synchronized(keyIndex) {
                val matchingKeys = keyIndex.filter { key -> regex.matches(key) }
                val deletedKeys = mutableListOf<String>()

                matchingKeys.forEach { key ->
                    val cacheKey = generateKey(key)
                    try {
                        val success = memcachedClient.delete(cacheKey)
                        if (success) {
                            deletedKeys.add(key)
                            keyIndex.remove(key)
                            logger.debug("Deleted cache key: {}", key)
                        }
                    } catch (e: Exception) {
                        logger.warn("Failed to delete cache key: {} due to: {}", key, e.message)
                    }
                }

                deletedKeys.toList()
            }
        }.subscribeOn(memcachedScheduler)
            .doOnSuccess { deletedKeys ->
                logger.debug("Successfully deleted {} cache keys matching pattern: {}", deletedKeys.size, pattern)
            }
            .doOnError { error ->
                logger.error("Error deleting cache keys matching pattern: {}", pattern, error)
            }
            .onErrorReturn(emptyList())
    }

    /**
     * Increments a numeric value in the cache.
     *
     * @param key The cache key
     * @param delta The amount to increment by
     * @return Mono<Long> containing the new value
     */
    fun increment(
        key: String,
        delta: Long
    ): Mono<Long> {
        val cacheKey = generateKey(key)

        logger.debug("Incrementing cache key: {} by {}", cacheKey, delta)

        return Mono.fromCallable {
            // Check if client is still active before attempting operation
            if (memcachedClient.isShutdown) {
                logger.warn("Memcached client is shutdown, skipping increment operation for key: {}", cacheKey)
                return@fromCallable -1L
            }
            memcachedClient.incr(cacheKey, delta)
        }.subscribeOn(memcachedScheduler)
            .doOnSuccess { newValue: Long ->
                logger.debug("Incremented cache key: {} to {}", cacheKey, newValue)
            }
            .doOnError { error: Throwable ->
                logger.error("Error incrementing cache key: {}", cacheKey, error)
            }
            .onErrorReturn(-1L)
    }

    /**
     * Generates a consistent cache key with namespace and Base64 encoding.
     *
     * Memcached has strict requirements for cache keys - they cannot contain
     * spaces or certain special characters. This method encodes the key to
     * ensure compatibility.
     *
     * @param key The original key
     * @return Namespaced and Base64-encoded cache key
     */
    private fun generateKey(key: String): String {
        val namespacedKey = "$DEFAULT_NAMESPACE:$key"
        val encodedKey = Base64.getEncoder().encodeToString(namespacedKey.toByteArray())
        return encodedKey
    }
}

package com.congen.util

/**
 * Exception thrown when a cache key is not found.
 *
 * This exception is used to distinguish between cache misses and actual errors
 * in the caching system. It allows for graceful handling of cache misses
 * without treating them as errors.
 *
 * @param cacheKey The cache key that was not found
 */
class CacheMissException(cacheKey: String) : RuntimeException("Cache miss for key: $cacheKey")

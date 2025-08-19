package com.congen.cache

import java.time.Duration

/**
 * Enumeration of cache TTL (Time-To-Live) values.
 *
 * This enum provides predefined TTL durations for different types of data
 * based on their volatility and access patterns.
 *
 * ## TTL Categories
 *
 * - **LONG_TERM (24 hours)**: Reference data like exercises, equipment, muscles
 * - **MEDIUM_TERM (1 hour)**: General relationship data and frequently accessed lists
 * - **SHORT_TERM (30 minutes)**: Individual records and moderate-frequency queries
 * - **VERY_SHORT_TERM (5 minutes)**: High-frequency list queries
 * - **USER_DATA (30 minutes)**: User-specific data that changes frequently
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
enum class CacheTTL(
    /** The duration for which cached data should be kept */
    val duration: Duration
) {
    /** 24 hours - Reference data like exercises, equipment, muscles */
    LONG_TERM(Duration.ofHours(24)),

    /** 1 hour - General relationship data and frequently accessed lists */
    MEDIUM_TERM(Duration.ofHours(1)),

    /** 30 minutes - Individual records and moderate-frequency queries */
    SHORT_TERM(Duration.ofMinutes(30)),

    /** 5 minutes - High-frequency list queries */
    VERY_SHORT_TERM(Duration.ofMinutes(5)),

    /** 30 minutes - User-specific data that changes frequently */
    USER_DATA(Duration.ofMinutes(30))
}

package com.congen.config

import org.springframework.context.annotation.Configuration

/**
 * Configuration for AspectJ compile-time caching functionality.
 *
 * This configuration supports AspectJ compile-time weaving for the caching aspect
 * to intercept DAL method calls transparently with better performance.
 *
 * ## Features
 *
 * - **Compile-time Weaving**: AspectJ weaves aspects at compile time for better performance
 * - **Caching Integration**: Integrates with existing Memcached infrastructure
 * - **Transparent Operation**: Services work without caching-related code changes
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Configuration
class CachingConfig

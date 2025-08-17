package com.congen.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy

/**
 * Configuration for Spring AOP caching functionality.
 *
 * This configuration enables AspectJ auto-proxy support for the caching aspect
 * to intercept DAL method calls transparently.
 *
 * ## Features
 *
 * - **AOP Support**: Enables AspectJ auto-proxy for method interception
 * - **Caching Integration**: Integrates with existing Memcached infrastructure
 * - **Transparent Operation**: Services work without caching-related code changes
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Configuration
@EnableAspectJAutoProxy
class CachingConfig

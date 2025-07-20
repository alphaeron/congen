package com.congen.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping

/**
 * Web configuration for the Congen API.
 *
 * This configuration class sets up the base API path prefix for all controllers.
 * All REST endpoints will be prefixed with `/api/v1/` to provide proper API versioning
 * and organization.
 *
 * ## API Versioning
 *
 * The API uses a versioned path structure:
 * - Base path: `/api/v1/`
 * - All controller endpoints are automatically prefixed
 * - Example: `/user/` becomes `/api/v1/user/`
 *
 * ## Benefits
 *
 * - **Versioning**: Clear API version identification
 * - **Organization**: Structured API hierarchy
 * - **Future-proofing**: Easy to add new API versions
 * - **Consistency**: All endpoints follow the same pattern
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Configuration
@EnableWebFlux
class WebConfig : WebFluxConfigurer {
    /**
     * Creates a custom RequestMappingHandlerMapping that adds the API base path prefix.
     *
     * This bean overrides the default RequestMappingHandlerMapping to automatically
     * prepend `/api/v1/` to all controller route mappings, providing a centralized
     * way to manage API versioning without modifying individual controllers.
     *
     * @return Custom RequestMappingHandlerMapping with API prefix support
     */
    @Bean
    @Primary
    fun requestMappingHandlerMapping(): RequestMappingHandlerMapping {
        return object : RequestMappingHandlerMapping() {
            override fun getMappingForMethod(
                method: java.lang.reflect.Method,
                handlerType: Class<*>
            ): org.springframework.web.reactive.result.method.RequestMappingInfo? {
                val mapping = super.getMappingForMethod(method, handlerType)
                return mapping?.let { addApiPrefix(it) }
            }

            private fun addApiPrefix(
                mapping: org.springframework.web.reactive.result.method.RequestMappingInfo
            ): org.springframework.web.reactive.result.method.RequestMappingInfo {
                val patterns = mapping.patternsCondition.patterns.map { "/api/v1$it" }

                return org.springframework.web.reactive.result.method.RequestMappingInfo.paths(*patterns.toTypedArray())
                    .methods(*mapping.methodsCondition.methods.toTypedArray())
                    .params(*mapping.paramsCondition.expressions.map { it.toString() }.toTypedArray())
                    .headers(*mapping.headersCondition.expressions.map { it.toString() }.toTypedArray())
                    .build()
            }
        }
    }

    /**
     * Configures custom converters for parameter binding.
     *
     * This method registers custom converters that allow Spring to automatically
     * convert URL parameters to complex types like enums.
     */
    override fun addFormatters(registry: org.springframework.format.FormatterRegistry) {
        super.addFormatters(registry)
        registry.addConverter(MovementTypeConverter())
    }
}

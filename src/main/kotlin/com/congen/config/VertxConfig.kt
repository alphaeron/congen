package com.congen.config

import io.vertx.core.Vertx
import io.vertx.core.json.jackson.DatabindCodec
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class VertxConfig {
    @Bean
    fun vertx(): Vertx {
        val vertx = Vertx.vertx()
        val mapper = DatabindCodec.mapper()
        val prettyMapper = DatabindCodec.prettyMapper()

        // Apply the same configuration as JacksonConfig
        JacksonConfig.configureObjectMapper(mapper)
        JacksonConfig.configureObjectMapper(prettyMapper)

        return vertx
    }
}

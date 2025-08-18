package com.congen.config

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import reactor.core.scheduler.Scheduler
import reactor.test.StepVerifier
import java.util.concurrent.TimeUnit
import reactor.core.publisher.Mono
import java.time.Duration

@ExtendWith(MockitoExtension::class)
class MemcachedConfigTest {

    @Test
    fun `should create memcached scheduler with custom properties`() {
        val properties = MemcachedProperties(
            host = "localhost",
            port = 11211,
            connectionPoolSize = 5,
            opTimeout = 3000L,
            maxQueuedNoReply = 500,
            schedulerThreadPoolSize = 3,
            schedulerQueueCapacity = 200,
            schedulerThreadNamePrefix = "test-scheduler"
        )

        val config = MemcachedConfig(properties)
        val scheduler = config.memcachedScheduler()

        // Verify scheduler is created and can execute tasks
        StepVerifier.create(
            Mono.fromCallable { "test" }
                .subscribeOn(scheduler)
                .timeout(Duration.ofSeconds(5))
        )
            .expectNext("test")
            .verifyComplete()

        // Clean up
        scheduler.dispose()
    }

    @Test
    fun `should create memcached scheduler with default properties`() {
        val properties = MemcachedProperties()
        val config = MemcachedConfig(properties)
        val scheduler = config.memcachedScheduler()

        // Verify scheduler is created and can execute tasks
        StepVerifier.create(
            Mono.fromCallable { "test" }
                .subscribeOn(scheduler)
                .timeout(Duration.ofSeconds(5))
        )
            .expectNext("test")
            .verifyComplete()

        // Clean up
        scheduler.dispose()
    }
}

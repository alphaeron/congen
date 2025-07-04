package com.congen.config

import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.util.Properties

@Configuration
class VersionConfig {
    val version: String
    val releaseId: String
    val buildTime: String
    val gitHash: String
    val gitBranch: String
    val gitDirty: Boolean

    init {
        // Load version properties from classpath if available
        val props = Properties()
        try {
            val resource = ClassPathResource("version.properties")
            if (resource.exists()) {
                resource.inputStream.use { props.load(it) }
            }
        } catch (e: Exception) {
            // If version.properties is not available, use default values
            // This can happen during development or if the build process hasn't run
        }

        // Set values with defaults if not found in properties
        version = props.getProperty("app.version", "0.0.1-SNAPSHOT")
        releaseId = props.getProperty("app.releaseId", "dev-release")
        buildTime = props.getProperty("app.buildTime", "unknown")
        gitHash = props.getProperty("app.gitHash", "unknown")
        gitBranch = props.getProperty("app.gitBranch", "unknown")
        gitDirty = props.getProperty("app.gitDirty", "false").toBoolean()
    }
}

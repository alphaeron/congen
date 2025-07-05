package com.congen.config

import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.util.Properties

/**
 * Configuration class for application version information.
 *
 * This class loads and provides access to version-related information about the application,
 * including version number, release ID, build time, and Git information. The version data
 * is loaded from a `version.properties` file on the classpath, with fallback default values
 * if the file is not available.
 *
 * This information is used for health checks, API documentation, and debugging purposes.
 *
 * @property version Application version number
 * @property releaseId Release identifier
 * @property buildTime Build timestamp
 * @property gitHash Git commit hash
 * @property gitBranch Git branch name
 * @property gitDirty Whether the Git working directory is dirty (has uncommitted changes)
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Configuration
class VersionConfig {
    /** Application version number. */
    val version: String

    /** Release identifier. */
    val releaseId: String

    /** Build timestamp. */
    val buildTime: String

    /** Git commit hash. */
    val gitHash: String

    /** Git branch name. */
    val gitBranch: String

    /** Whether the Git working directory is dirty (has uncommitted changes). */
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

package com.congen.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

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
@ConfigurationProperties(prefix = "app")
data class VersionProperties(
    var version: String = "0.0.1-SNAPSHOT",
    var releaseId: String = "dev-release",
    var buildTime: String = "unknown",
    var gitHash: String = "unknown",
    var gitBranch: String = "unknown",
    var gitDirty: Boolean = false
)

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
 * @property versionProperties The version-related properties loaded from configuration.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(VersionProperties::class)
class VersionConfig(
    /**
     * The version-related properties loaded from configuration.
     */
    val versionProperties: VersionProperties
) {
    /** Application version number. */
    val version: String = versionProperties.version

    /** Release identifier. */
    val releaseId: String = versionProperties.releaseId

    /** Build timestamp. */
    val buildTime: String = versionProperties.buildTime

    /** Git commit hash. */
    val gitHash: String = versionProperties.gitHash

    /** Git branch name. */
    val gitBranch: String = versionProperties.gitBranch

    /** Whether the Git working directory is dirty (has uncommitted changes). */
    val gitDirty: Boolean = versionProperties.gitDirty
}

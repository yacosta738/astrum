package com.astrum.talentum

import com.astrum.common.domain.Generated
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.stream.Collectors
import java.util.stream.Stream


object ApplicationStartupTraces {
    private val SEPARATOR = "-".repeat(58)
    private const val BREAK = "\n"
    private val log = LoggerFactory.getLogger(ApplicationStartupTracesBuilder::class.java)
    fun of(environment: Environment): String {
        return ApplicationStartupTracesBuilder()
            .append(BREAK)
            .appendSeparator()
            .append(applicationRunningTrace(environment))
            .append(localUrl(environment))
            .append(externalUrl(environment))
            .append(profilesTrace(environment))
            .appendSeparator()
            .append(configServer(environment))
            .build()
    }

    private fun applicationRunningTrace(environment: Environment): String {
        val applicationName: String = environment.getProperty("spring.application.name", "")
        return if (applicationName.isBlank()) {
            "Application is running!"
        } else StringBuilder().append("Application '").append(applicationName)
            .append("' is running!").toString()
    }

    private fun localUrl(environment: Environment): String {
        return url("Local", "localhost", environment)
    }

    private fun externalUrl(environment: Environment): String {
        return url("External", hostAddress(), environment)
    }

    private fun url(type: String, host: String, environment: Environment): String {
        return if (notWebEnvironment(environment)) {
            "\uD83D\uDE80 $type: \t$host"
        } else StringBuilder()
            .append(type)
            .append(": \t")
            .append(protocol(environment))
            .append("://")
            .append(host)
            .append(":")
            .append(port(environment))
            .append(contextPath(environment))
            .toString()
    }

    private fun notWebEnvironment(environment: Environment): Boolean {
        return environment.getProperty("server.port")?.isBlank() ?: true
    }

    private fun protocol(environment: Environment): String {
        return if (noKeyStore(environment)) {
            "http"
        } else "https"
    }

    private fun noKeyStore(environment: Environment): Boolean {
        return environment.getProperty("server.ssl.key-store")?.isBlank() ?: true
    }

    private fun port(environment: Environment): String {
        return environment.getProperty("server.port", "8080")
    }

    private fun profilesTrace(environment: Environment): String {
        val profiles: Array<String> = environment.activeProfiles
        return if (profiles.isEmpty()) {
            " No active profile set, falling back to default profiles: default"
        } else StringBuilder().append("Profile(s): \t")
            .append(Stream.of(*profiles).collect(Collectors.joining(", "))).toString()
    }

    @Generated(reason = "Hard to test implement detail error management")
    private fun hostAddress(): String {
        try {
            return InetAddress.getLocalHost().hostAddress
        } catch (e: UnknownHostException) {
            log.warn("The host name could not be determined, using `localhost` as fallback")
        }
        return "localhost"
    }

    private fun contextPath(environment: Environment): String {
        val contextPath: String = environment.getProperty("server.servlet.context-path", "/")
        return contextPath.takeIf { it.isNotBlank() } ?: "/"
    }

    private fun configServer(environment: Environment): String {
        val configServer: String = environment.getProperty("configserver.status", "")
        return if (configServer.isBlank()) {
            "Config Server: \tNot found or not setup for this application"
        } else StringBuilder().append("Config Server: ").append(configServer).append(BREAK).append(
            SEPARATOR
        ).append(BREAK).toString()
    }

    private class ApplicationStartupTracesBuilder {
        private val trace = StringBuilder()
        fun appendSeparator(): ApplicationStartupTracesBuilder {
            trace.append(SEPARATOR).append(BREAK)
            return this
        }

        fun append(line: String): ApplicationStartupTracesBuilder {
            trace.append(SPACER).append(line).append(BREAK)
            return this
        }

        fun build(): String {
            return trace.toString()
        }

        companion object {
            private const val SPACER = "  "
        }
    }
}

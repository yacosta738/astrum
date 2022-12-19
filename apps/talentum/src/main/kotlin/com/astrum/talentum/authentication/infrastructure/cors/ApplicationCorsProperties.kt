package com.astrum.talentum.authentication.infrastructure.cors

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated

@Validated
@Configuration
@ConfigurationProperties(prefix = "app.cors", ignoreUnknownFields = false)
class ApplicationCorsProperties {
    var allowedOrigins: List<String> = ArrayList()
    var allowedMethods: List<String> = listOf("*")
    var allowedHeaders: List<String> = listOf("*")
    var exposedHeaders: List<String> = listOf("Authorization")
    var allowCredentials: Boolean = false
    var maxAge: Long = 3600
}

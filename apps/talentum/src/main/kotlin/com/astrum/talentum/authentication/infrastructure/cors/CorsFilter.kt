package com.astrum.talentum.authentication.infrastructure.cors

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter


@Configuration
class CorsConfig(private val corsProperties: ApplicationCorsProperties) {
    @Bean
    fun corsFilter(): CorsFilter {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration()
        config.allowCredentials = true
        config.allowedOriginPatterns = corsProperties.allowedOrigins
        config.allowedMethods = corsProperties.allowedMethods
        config.allowedHeaders = corsProperties.allowedHeaders
        config.exposedHeaders = corsProperties.exposedHeaders
        config.maxAge = corsProperties.maxAge
        config.allowCredentials = corsProperties.allowCredentials
        source.registerCorsConfiguration("/**", config)
        return CorsFilter(source)
    }
}
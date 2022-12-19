package com.astrum.talentum.authentication.infrastructure

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated

class OAuth2 {
    var audience: List<String> = ArrayList()
}

@Validated
@Configuration
@ConfigurationProperties(prefix = "app.security", ignoreUnknownFields = false)
class ApplicationSecurityProperties {
    val oauth2 = OAuth2()
    var contentSecurityPolicy = CONTENT_SECURITY_POLICY

    companion object {
        private val CONTENT_SECURITY_POLICY = """
    default-src 'self'; frame-src 'self' data:; script-src 'self' 'unsafe-inline' 'unsafe-eval' https://storage.googleapis.com; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; img-src 'self' data:; font-src 'self' data: https://fonts.gstatic.com;
    """.trimIndent()
    }
}

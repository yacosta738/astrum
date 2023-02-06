package com.astrum.authentication.infrastructure.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import java.net.URI


/**
 * Here are defaults:
 *
 * <pre>
 * com.astrum.springaddons.security.issuers[0].location=https://localhost:8443/realms/master
 * com.astrum.springaddons.security.issuers[0].authorities.claims=realm_access.roles,permissions
 * com.astrum.springaddons.security.issuers[0].authorities.prefix=
 * com.astrum.springaddons.security.issuers[0].authorities.caze=
 * com.astrum.springaddons.security.cors[0].path=/ **
 * com.astrum.springaddons.security.cors[0].allowed-origins=*
 * com.astrum.springaddons.security.cors[0].allowedOrigins=*
 * com.astrum.springaddons.security.cors[0].allowedMethods=*
 * com.astrum.springaddons.security.cors[0].allowedHeaders=*
 * com.astrum.springaddons.security.cors[0].exposedHeaders=*
 * com.astrum.springaddons.security.csrf-enabled=true
 * com.astrum.springaddons.security.permit-all=
 * com.astrum.springaddons.security.redirect-to-login-if-unauthorized-on-restricted-content=true
 * com.astrum.springaddons.security.statless-sessions=true
</pre> *
 *
 * @author Yuniel Acosta
 */
@AutoConfiguration
@ConfigurationProperties(prefix = "com.astrum.springaddons.security")
data class SpringAddonsSecurityProperties(
    var issuers: Array<IssuerProperties> = arrayOf(),
    var cors: Array<CorsProperties> = arrayOf(),
    var permitAll: Array<String> = arrayOf(
        "/actuator/**",
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/webjars/swagger-ui/**",
        "/favicon.ico"
    ),
    var redirectToLoginIfUnauthorizedOnRestrictedContent: Boolean = false,
    var statelessSessions: Boolean = true,
    var csrf: Csrf = Csrf.DEFAULT
) {
    @Autowired
    constructor() : this(arrayOf(), arrayOf(), arrayOf(), false, true, Csrf.DEFAULT)

    data class CorsProperties(
        var path: String = "/**",
        var allowedOrigins: List<String> = listOf("*"),
        var allowedMethods: List<String> = listOf("*"),
        var allowedHeaders: List<String> = listOf("*"),
        var exposedHeaders: List<String> = listOf("*")
    )

    data class IssuerProperties(
        var location: URI? = null,
        var jwkSetUri: URI? = null,
        var authorities: SimpleAuthoritiesMappingProperties = SimpleAuthoritiesMappingProperties()
    )

    data class SimpleAuthoritiesMappingProperties(
        var claims: Array<String> = arrayOf("realm_access.roles"),
        var prefix: String = "",
        var caze: Case = Case.UNCHANGED
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is SimpleAuthoritiesMappingProperties) return false

            if (!claims.contentEquals(other.claims)) return false
            if (prefix != other.prefix) return false
            if (caze != other.caze) return false

            return true
        }

        override fun hashCode(): Int {
            var result = claims.contentHashCode()
            result = 31 * result + prefix.hashCode()
            result = 31 * result + caze.hashCode()
            return result
        }

        override fun toString(): String {
            return "SimpleAuthoritiesMappingProperties(claims=${claims.contentToString()}, prefix='$prefix', caze=$caze)"
        }

    }

    enum class Case {
        UNCHANGED, UPPER, LOWER
    }

    /**
     *
     *  * DEFAULT switches to DISABLED if statelessSessions is true and Spring default otherwise.
     *  * DISABLE disables CSRF protection.
     *  * SESSION stores CSRF token in servlet session or reactive web-session (makes no sense if session-management is "stateless").
     *  * COOKIE_HTTP_ONLY stores CSRF in a http-only XSRF-TOKEN cookie (not accessible from rich client apps).
     *  * COOKIE_ACCESSIBLE_FROM_JS stores CSRF in a XSRF-TOKEN cookie that is readable by rich client apps.
     *
     *
     * @author Yuniel Acosta
     */
    enum class Csrf {
        DEFAULT, DISABLE, SESSION, COOKIE_HTTP_ONLY, COOKIE_ACCESSIBLE_FROM_JS
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SpringAddonsSecurityProperties) return false

        if (!issuers.contentEquals(other.issuers)) return false
        if (!cors.contentEquals(other.cors)) return false
        if (!permitAll.contentEquals(other.permitAll)) return false
        if (redirectToLoginIfUnauthorizedOnRestrictedContent != other.redirectToLoginIfUnauthorizedOnRestrictedContent) return false
        if (statelessSessions != other.statelessSessions) return false
        if (csrf != other.csrf) return false

        return true
    }

    override fun hashCode(): Int {
        var result = issuers.contentHashCode()
        result = 31 * result + cors.contentHashCode()
        result = 31 * result + permitAll.contentHashCode()
        result = 31 * result + redirectToLoginIfUnauthorizedOnRestrictedContent.hashCode()
        result = 31 * result + statelessSessions.hashCode()
        result = 31 * result + csrf.hashCode()
        return result
    }
}

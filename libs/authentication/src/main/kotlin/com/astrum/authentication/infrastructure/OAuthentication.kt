package com.astrum.authentication.infrastructure

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import java.io.Serializable
import java.util.*


class OAuthentication<T>(
    claims: T,
    authorities: Collection<GrantedAuthority>,
    tokenString: String
) :
    AbstractAuthenticationToken(authorities),
    OAuth2AuthenticatedPrincipal where T : Map<String, Any>, T : Serializable {
    private val tokenString: String
    val claims: T

    /**
     * @param claims      claim-set of any-type
     * @param authorities
     * @param tokenString original encoded JWT string (in case resource-server needs to forward user ID to secured micro-services)
     */
    init {
        super.setAuthenticated(true)
        super.setDetails(claims)
        this.claims = claims
        this.tokenString = Optional.ofNullable(tokenString).map { ts ->
            if (ts.lowercase(Locale.getDefault()).startsWith("bearer ")) ts.substring(7) else ts
        }.orElse(null)
    }

    override fun setDetails(details: Any) {
        // Do nothing until spring-security 6.1.0 and https://github.com/spring-projects/spring-security/issues/11822 fix is released
        // throw new RuntimeException("OAuthentication details are immutable");
    }

    override fun setAuthenticated(isAuthenticated: Boolean) {
        throw RuntimeException("OAuthentication authentication status is immutable")
    }

    override fun getCredentials(): String {
        return tokenString
    }

    override fun getPrincipal(): T {
        return claims
    }

    override fun getAttributes(): T {
        return claims
    }

    val bearerHeader: String?
        get() = if (tokenString.isBlank()) null else "Bearer $tokenString"

    companion object {
        private const val serialVersionUID = -2827891205034221389L
    }
}

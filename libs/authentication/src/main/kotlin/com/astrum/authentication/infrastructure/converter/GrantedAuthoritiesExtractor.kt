package com.astrum.authentication.infrastructure.converter

import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt

internal class GrantedAuthoritiesExtractor : Converter<Jwt, Collection<GrantedAuthority>> {
    companion object {
        private const val CLAIMS_NAMESPACE = "https://www.astrum.com/"
    }

    @Suppress("UNCHECKED_CAST")
    override fun convert(jwt: Jwt): Collection<GrantedAuthority> {
        val authorities: List<Any> = jwt.claims
            .getOrDefault(
                "groups",
                jwt.claims.getOrDefault(
                    "roles",
                    jwt.claims.getOrDefault(CLAIMS_NAMESPACE, emptyList<Any>())
                )
            ) as List<Any>
        return authorities
            .map { it.toString() }
            .filter { role: String ->
                role.startsWith(
                    "ROLE_"
                )
            }
            .map { SimpleGrantedAuthority(it) }
    }
}
package com.astrum.authentication.infrastructure.converter

import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt

internal class GrantedAuthoritiesExtractor : Converter<Jwt, Collection<GrantedAuthority>> {

    @Suppress("UNCHECKED_CAST")
    override fun convert(jwt: Jwt): Collection<GrantedAuthority> =
        extractAuthorityFromClaims(jwt.claims)

    private fun extractAuthorityFromClaims(claims: Map<String, Any>): List<GrantedAuthority> {
        return mapRolesToGrantedAuthorities(getRolesFromClaims(claims))
    }

    @Suppress("UNCHECKED_CAST")
    private fun getRolesFromClaims(claims: Map<String, Any>): Collection<String> {
        return when (val test = claims.getOrDefault(
            "groups",
            claims.getOrDefault(
                "roles",
                claims.getOrDefault(
                    "${com.astrum.authentication.infrastructure.config.CLAIMS_NAMESPACE}roles",
                    listOf<String>()
                )
            )
        )) {
            is String -> listOf(test)
            else -> test as Collection<String>
        }
    }

    private fun mapRolesToGrantedAuthorities(roles: Collection<String>): List<GrantedAuthority> {
        return roles
            .filter { it.startsWith("ROLE_") }
            .map { SimpleGrantedAuthority(it) }
    }
}

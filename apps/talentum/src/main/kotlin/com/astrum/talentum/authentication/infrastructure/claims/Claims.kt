package com.astrum.talentum.authentication.infrastructure.claims

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.stream.Collectors


internal object Claims {
    const val CLAIMS_NAMESPACE = "https://www.astrum.com/"
    fun extractAuthorityFromClaims(claims: Map<String, Any>): List<GrantedAuthority> =
        mapRolesToGrantedAuthorities(getRolesFromClaims(claims))

    @Suppress("UNCHECKED_CAST")
    private fun getRolesFromClaims(claims: Map<String, Any>): Collection<String> {
        return claims.getOrDefault(
            "groups",
            claims.getOrDefault(
                "roles",
                claims.getOrDefault(CLAIMS_NAMESPACE + "roles", emptyList<String>())
            )
        ) as Collection<String>
    }

    private fun mapRolesToGrantedAuthorities(roles: Collection<String>): List<GrantedAuthority> {
        return roles.stream().filter { role: String ->
            role.startsWith(
                "ROLE_"
            )
        }.map { role: String ->
            SimpleGrantedAuthority(
                role
            )
        }.collect(Collectors.toList())
    }
}

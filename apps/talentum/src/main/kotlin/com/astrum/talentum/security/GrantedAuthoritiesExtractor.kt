package com.astrum.talentum.security

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtGrantedAuthoritiesConverterAdapter
import reactor.core.publisher.Mono
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream


class KeycloakGrantedAuthoritiesConverter(private val clientId: String) :
    Converter<Jwt, Collection<GrantedAuthority>> {
    private val defaultAuthoritiesConverter: Converter<Jwt, Collection<GrantedAuthority>> =
        JwtGrantedAuthoritiesConverter()

    override fun convert(jwt: Jwt): Collection<GrantedAuthority> {
        val realmRoles = realmRoles(jwt)
        val clientRoles = clientRoles(jwt, clientId)
        val authorities: MutableCollection<GrantedAuthority> =
            Stream.concat(realmRoles.stream(), clientRoles.stream())
                .map { role: String -> SimpleGrantedAuthority(role) }
                .collect(Collectors.toSet())
        authorities.addAll(defaultGrantedAuthorities(jwt))
        return authorities
    }

    private fun defaultGrantedAuthorities(jwt: Jwt): Collection<GrantedAuthority> {
        return Optional.ofNullable(defaultAuthoritiesConverter.convert(jwt))
            .orElse(emptySet())
    }

    @Suppress("UNCHECKED_CAST")
    private fun realmRoles(jwt: Jwt): List<String> {
        return Optional.ofNullable(jwt.getClaimAsMap(CLAIM_REALM_ACCESS))
            .map { realmAccess -> realmAccess[ROLES] as List<String> }
            .orElse(emptyList())
    }

    @Suppress("UNCHECKED_CAST")
    private fun clientRoles(jwt: Jwt, clientId: String): List<String> {
        return if (clientId.isEmpty()) {
            emptyList()
        } else Optional.ofNullable(jwt.getClaimAsMap(RESOURCE_ACCESS))
            .map { resourceAccess -> resourceAccess[clientId] as Map<String, List<String>> }
            .map { clientAccess -> clientAccess[ROLES] }
            .orElse(emptyList()) as List<String>
    }


    companion object {
        private const val ROLES = "roles"
        private const val CLAIM_REALM_ACCESS = "realm_access"
        private const val RESOURCE_ACCESS = "resource_access"
    }
}

/**
 * @see org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter
 */
class ReactiveKeycloakJwtAuthenticationConverter(
    private val jwtGrantedAuthoritiesConverter: Converter<Jwt, Collection<GrantedAuthority>>
) : Converter<Jwt, Mono<AbstractAuthenticationToken>> {
    companion object {
        private const val USERNAME_CLAIM = "preferred_username"
    }

    private val reactiveJwtGrantedAuthoritiesConverter =
        ReactiveJwtGrantedAuthoritiesConverterAdapter(jwtGrantedAuthoritiesConverter)


    override fun convert(jwt: Jwt): Mono<AbstractAuthenticationToken> {
        return reactiveJwtGrantedAuthoritiesConverter.convert(jwt)
            ?.collectList()
            ?.map { authorities -> JwtAuthenticationToken(jwt, authorities, extractUsername(jwt)) }
            ?: Mono.empty()
    }

    private fun extractUsername(jwt: Jwt): String {
        return if (jwt.hasClaim(USERNAME_CLAIM)) {
            jwt.getClaimAsString(USERNAME_CLAIM)
        } else {
            jwt.subject
        }
    }
}

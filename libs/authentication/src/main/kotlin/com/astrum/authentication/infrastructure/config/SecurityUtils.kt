@file:JvmName("SecurityUtils")
@file:Suppress("CAST_NEVER_SUCCEEDS")

package com.astrum.authentication.infrastructure.config

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import reactor.core.publisher.Mono
import java.util.*

const val CLAIMS_NAMESPACE = "https://www.jhipster.tech/"

/**
 * Get the login of the current user.
 *
 * @return the login of the current user.
 */
fun getCurrentUserLogin(): Mono<String> =
    ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .flatMap { Mono.justOrEmpty(extractPrincipal(it)) }

fun extractPrincipal(authentication: Authentication?): String? {

    if (authentication == null) {
        return null
    }

    return when (val principal = authentication.principal) {
        is UserDetails -> principal.username
        is JwtAuthenticationToken -> (authentication as JwtAuthenticationToken).token.claims as String
        is DefaultOidcUser -> {
            if (principal.attributes.containsKey("preferred_username")) {
                principal.attributes["preferred_username"].toString()
            } else {
                null
            }
        }

        is String -> principal
        else -> null
    }
}

/**
 * Check if a user is authenticated.
 *
 * @return true if the user is authenticated, false otherwise.
 */
fun isAuthenticated(): Mono<Boolean> {
    return ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .map(Authentication::getAuthorities)
        .map { authorities ->
            authorities
                .map(GrantedAuthority::getAuthority)
                .none { it == ANONYMOUS }
        }
}

/**
 * Checks if the current user has any of the authorities.
 *
 * @param authorities the authorities to check.
 * @return true if the current user has any of the authorities, false otherwise.
 */
fun hasCurrentUserAnyOfAuthorities(vararg authorities: String): Mono<Boolean> {
    return ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .map(Authentication::getAuthorities)
        .map { auth ->
            auth
                .map(GrantedAuthority::getAuthority)
                .any { authorities.contains(it) }
        }
}

/**
 * Checks if the current user has none of the authorities.
 *
 * @param authorities the authorities to check.
 * @return true if the current user has none of the authorities, false otherwise.
 */
fun hasCurrentUserNoneOfAuthorities(vararg authorities: String): Mono<Boolean> {
    return hasCurrentUserAnyOfAuthorities(*authorities).map { !it }
}

/**
 * Checks if the current user has a specific authority.
 *
 * @param authority the authority to check.
 * @return true if the current user has the authority, false otherwise.
 */
fun hasCurrentUserThisAuthority(authority: String): Mono<Boolean> {
    return hasCurrentUserAnyOfAuthorities(authority)
}

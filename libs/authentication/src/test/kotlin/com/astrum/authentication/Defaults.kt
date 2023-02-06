package com.astrum.authentication

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.*
import java.util.stream.Collectors


/**
 * @author Yuniel Acosta
 */
object Defaults {
    const val SUBJECT = "user"
    const val AUTH_NAME = "user"
    val SCOPES = listOf<String>()
    val AUTHORITIES = listOf<String>()
    const val BEARER_TOKEN_VALUE = "Bearer test token"
    const val JWT_VALUE = "jwt.test.token"
    val GRANTED_AUTHORITIES: Set<GrantedAuthority> =
        Collections.unmodifiableSet(AUTHORITIES.stream().map { role: String ->
            SimpleGrantedAuthority(
                role
            )
        }.collect(Collectors.toSet()))
}

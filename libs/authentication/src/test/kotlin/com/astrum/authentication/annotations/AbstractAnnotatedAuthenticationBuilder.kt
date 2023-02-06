package com.astrum.authentication.annotations

import com.astrum.authentication.infrastructure.ModifiableClaimSet
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContextFactory
import java.util.stream.Collectors
import java.util.stream.Stream


abstract class AbstractAnnotatedAuthenticationBuilder<A : Annotation, T : Authentication> :
    WithSecurityContextFactory<A> {

    protected abstract fun authentication(annotation: A): T

    override fun createSecurityContext(annotation: A): SecurityContext {
        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = authentication(annotation)
        return context
    }

    fun authorities(vararg authorities: String): Set<GrantedAuthority> {
        return Stream.of(*authorities).map { SimpleGrantedAuthority(it) }
            .collect(Collectors.toSet())
    }

    fun claims(annotation: OpenIdClaims): ModifiableClaimSet {
        return OpenIdClaims.Builder.of(annotation)
    }
}

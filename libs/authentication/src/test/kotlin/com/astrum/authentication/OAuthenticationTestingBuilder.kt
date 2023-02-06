package com.astrum.authentication

import com.astrum.authentication.infrastructure.OAuthentication
import com.astrum.authentication.infrastructure.OpenidClaimSet
import com.astrum.authentication.infrastructure.domain.AuthenticationBuilder
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.stream.*


open class OAuthenticationTestingBuilder<T : OAuthenticationTestingBuilder<T>> :
    AuthenticationBuilder<OAuthentication<OpenidClaimSet>> {
    private val tokenBuilder: OpenidClaimSetBuilder = OpenidClaimSetBuilder().apply {
        subject(Defaults.SUBJECT)
        name(Defaults.AUTH_NAME)
    }
    private var authorities: MutableSet<String> = HashSet(Defaults.AUTHORITIES)
    private var bearerString = "machin.truc.chose"

    override fun build(): OAuthentication<OpenidClaimSet> {
        return OAuthentication(
            tokenBuilder.build(),
            authorities.map { SimpleGrantedAuthority(it) }.toSet(),
            bearerString
        )
    }

    fun authorities(vararg authorities: String): T {
        this.authorities.clear()
        this.authorities.addAll(authorities.toList())
        return downcast()
    }

    fun token(tokenBuilderConsumer: (OpenidClaimSetBuilder) -> Unit): T {
        tokenBuilderConsumer(tokenBuilder)
        return downcast()
    }

    fun bearerString(bearerString: String): T {
        this.bearerString = bearerString
        return downcast()
    }

    @Suppress("UNCHECKED_CAST")
    protected fun downcast(): T {
        return this as T
    }
}

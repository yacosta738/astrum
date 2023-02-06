package com.astrum.authentication.infrastructure.domain

import org.springframework.security.authentication.AbstractAuthenticationToken
import reactor.core.publisher.Mono

interface OAuth2AuthenticationFactory {
    fun build(
        bearerString: String,
        claims: Map<String, Any>
    ): Mono<AbstractAuthenticationToken>
}

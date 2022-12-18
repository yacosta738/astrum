package com.acosta.education.message.dashboard

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono


@RestController
@RequestMapping("/api/me")
class AboutMeController {
    @GetMapping
    fun claims(@AuthenticationPrincipal auth: JwtAuthenticationToken): Mono<Map<String, Any>> {
        return Mono.just(auth.tokenAttributes)
    }

    @GetMapping("/token")
    fun token(@AuthenticationPrincipal auth: JwtAuthenticationToken): Mono<String> {
        return Mono.just(auth.token.tokenValue)
    }

    @GetMapping("/role_admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun role_admin(): Mono<String> {
        return Mono.just("ROLE_ADMIN")
    }

    @GetMapping("/scope_messages_read")
    @PreAuthorize("hasAuthority('SCOPE_MESSAGES:READ')")
    fun scope_api_me_read(): Mono<String> {
        return Mono.just("You have 'MESSAGES:READ' scope")
    }
}

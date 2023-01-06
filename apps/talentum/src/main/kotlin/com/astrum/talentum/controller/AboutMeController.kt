package com.astrum.talentum.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono


@RestController
@RequestMapping("/api/profile")
class AboutMeController {
    @GetMapping
    fun claims(@AuthenticationPrincipal auth: JwtAuthenticationToken): Mono<Map<String, Any>> {
        return Mono.just(auth.tokenAttributes)
    }

    @GetMapping("/token")
    fun token(@AuthenticationPrincipal auth: JwtAuthenticationToken): Mono<String> {
        return Mono.just(auth.token.tokenValue)
    }

    @GetMapping("/role/admin")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    fun roleAdmin(): Mono<String> {
        return Mono.just("ROLE_ADMIN")
    }

    @GetMapping("/scope/messages/read")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun scopeApiMeRead(): Mono<String> {
        return Mono.just("You have 'MESSAGES:READ' scope")
    }
}

package com.acosta.education.message.dashboard

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping

import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.security.Principal


@RestController
class HelloController {
    @GetMapping("hello")
    fun hello(@AuthenticationPrincipal auth: Principal): Mono<String> {
        return Mono.just(String.format("Hello %s!", auth.name))
    }
}
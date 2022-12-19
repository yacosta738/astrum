package com.astrum.talentum.config


import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.web.server.SecurityWebFilterChain
import reactor.core.publisher.Mono


@ConditionalOnProperty(name = ["spring.security.oauth2.resourceserver.jwt.issuer-uri"])
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfiguration {
    @Bean
    fun springSecurityFilterChain(
        http: ServerHttpSecurity,
        jwtAuthenticationConverter: Converter<Jwt, Mono<AbstractAuthenticationToken>>
    ): SecurityWebFilterChain {
        // @formatter:off
        http.authorizeExchange()
            .pathMatchers("/hello/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
            .anyExchange().authenticated()
            .and()
            .oauth2ResourceServer()
            .jwt()
            .jwtAuthenticationConverter(jwtAuthenticationConverter)
        // @formatter:on
        return http.build()
    }
}
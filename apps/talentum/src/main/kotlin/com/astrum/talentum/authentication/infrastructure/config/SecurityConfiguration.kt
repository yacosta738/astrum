package com.astrum.talentum.authentication.infrastructure.config

import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec
import org.springframework.security.config.web.server.ServerHttpSecurity.OAuth2ResourceServerSpec
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authorization.HttpStatusServerAccessDeniedHandler

@EnableWebFluxSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityConfiguration {
    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http.authorizeExchange { exchanges: AuthorizeExchangeSpec ->
            exchanges
                .pathMatchers("/ping").permitAll()
                .pathMatchers("/hello").authenticated()
                .pathMatchers("/api/profile").authenticated()
                .pathMatchers("/api/profile/token").authenticated()
                .pathMatchers("/api/profile/role_admin").hasAnyAuthority("ROLE_ADMIN")
                .pathMatchers("/api/profile/scope_messages_read")
                .hasAnyAuthority("SCOPE_MESSAGES:READ")
        }.oauth2ResourceServer { oauth2: OAuth2ResourceServerSpec ->
            oauth2
                .jwt(withDefaults())
                .and()
                .exceptionHandling()
                .accessDeniedHandler(HttpStatusServerAccessDeniedHandler(HttpStatus.OK))
        }
        return http.build()
    }
}
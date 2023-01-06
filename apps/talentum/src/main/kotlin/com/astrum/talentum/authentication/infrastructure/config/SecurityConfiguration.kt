package com.astrum.talentum.authentication.infrastructure.config

import org.springframework.context.annotation.*
import org.springframework.core.convert.converter.*
import org.springframework.http.*
import org.springframework.security.authentication.*
import org.springframework.security.config.Customizer.*
import org.springframework.security.config.annotation.method.configuration.*
import org.springframework.security.config.annotation.web.reactive.*
import org.springframework.security.config.web.server.*
import org.springframework.security.config.web.server.ServerHttpSecurity.*
import org.springframework.security.core.*
import org.springframework.security.core.authority.*
import org.springframework.security.oauth2.jwt.*
import org.springframework.security.oauth2.server.resource.authentication.*
import org.springframework.security.web.server.*
import org.springframework.security.web.server.authorization.*
import reactor.core.publisher.*


@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfiguration {

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        // @formatter:off
        return http {
            csrf {
                disable()
            }
            authorizeExchange {
                authorize("/ping", permitAll)
                authorize("/swagger-ui.html", permitAll)
                authorize("/swagger-ui/**", permitAll)
                authorize(
                    "/v3/api-docs/**",
                    permitAll
                ) // TODO: add a list properties with allowed paths
                authorize("/hello", authenticated)
                authorize("/api/profile", authenticated)
                authorize("/api/profile/token", authenticated)
                authorize("/api/profile/role/admin", hasAnyRole("ADMIN"))
                authorize("/api/profile/scope/messages/read", hasAnyAuthority("ROLE_ADMIN"))
                authorize(anyExchange, authenticated)
            }
            oauth2ResourceServer {
                jwt {
                    jwtAuthenticationConverter = grantedAuthoritiesExtractor()
                }
            }
        }
        // @formatter:on
    }

    fun grantedAuthoritiesExtractor(): Converter<Jwt, Mono<AbstractAuthenticationToken>> {
        val jwtAuthenticationConverter = JwtAuthenticationConverter()
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(GrantedAuthoritiesExtractor())
        return ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter)
    }

    internal class GrantedAuthoritiesExtractor : Converter<Jwt, Collection<GrantedAuthority>> {
        companion object {
            private const val CLAIMS_NAMESPACE = "https://www.astrum.com/"
        }

        @Suppress("UNCHECKED_CAST")
        override fun convert(jwt: Jwt): Collection<GrantedAuthority> {
            val authorities: List<Any> = jwt.claims
                .getOrDefault(
                    "groups",
                    jwt.claims.getOrDefault(
                        "roles",
                        jwt.claims.getOrDefault(CLAIMS_NAMESPACE, emptyList<Any>())
                    )
                ) as List<Any>
            return authorities
                .map { it.toString() }
                .filter { role: String ->
                    role.startsWith(
                        "ROLE_"
                    )
                }
                .map { SimpleGrantedAuthority(it) }
        }
    }
}
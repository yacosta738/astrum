package com.astrum.authentication.infrastructure.config

import com.astrum.authentication.infrastructure.OAuthentication
import com.astrum.authentication.infrastructure.OpenidClaimSet
import com.astrum.authentication.infrastructure.domain.AuthorizeExchangeSpecPostProcessor
import com.astrum.authentication.infrastructure.domain.OAuth2AuthenticationFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec
import org.springframework.security.core.GrantedAuthority
import reactor.core.publisher.Mono


@Configuration
@EnableReactiveMethodSecurity
class SecurityConfiguration {
    @Bean
    fun authenticationFactory(authoritiesConverter: Converter<Map<String, Any>, Collection<GrantedAuthority>>): OAuth2AuthenticationFactory {
        return object : OAuth2AuthenticationFactory {
            override fun build(
                bearerString: String,
                claims: Map<String, Any>
            ): Mono<AbstractAuthenticationToken> {
                return Mono.just(
                    abstractAuthenticationToken(authoritiesConverter, claims, bearerString)
                )
            }
        }
    }

    private fun abstractAuthenticationToken(
        authoritiesConverter: Converter<Map<String, Any>, Collection<GrantedAuthority>>,
        claims: Map<String, Any>,
        bearerString: String
    ) = authoritiesConverter.convert(claims)?.let {
        OAuthentication(
            OpenidClaimSet(claims),
            it,
            bearerString
        )
    } ?: OAuthentication(
        OpenidClaimSet(claims),
        emptyList(),
        bearerString
    )

    @Bean
    fun authorizeExchangeSpecPostProcessor(): AuthorizeExchangeSpecPostProcessor {
        return object : AuthorizeExchangeSpecPostProcessor {
            override fun authorizeHttpRequests(spec: AuthorizeExchangeSpec): AuthorizeExchangeSpec {
                return spec
                    .pathMatchers(HttpMethod.GET, "/actuator/**")
                    .hasAuthority("OBSERVABILITY:read")
                    .pathMatchers("/actuator/**")
                    .hasAuthority("OBSERVABILITY:write")
                    .anyExchange().authenticated()
            }
        }
    }
}

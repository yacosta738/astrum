package com.astrum.authentication.infrastructure.config

import com.astrum.authentication.infrastructure.ApplicationSecurityProperties
import com.astrum.authentication.infrastructure.converter.GrantedAuthoritiesExtractor
import org.springframework.beans.factory.annotation.Value
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
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.oauth2.jwt.*
import org.springframework.security.oauth2.server.resource.authentication.*
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter
import org.springframework.security.web.server.*
import org.springframework.security.web.server.authorization.*
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter.*
import org.springframework.web.cors.reactive.CorsWebFilter
import org.zalando.problem.spring.webflux.advice.security.SecurityProblemSupport
import reactor.core.publisher.*
import java.util.function.*

private const val PERMISSIONS_POLICY =
    "camera=(), fullscreen=(self), geolocation=(), gyroscope=(), magnetometer=(), microphone=(), midi=(), payment=(), sync-xhr=()"

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@Import(SecurityProblemSupport::class)
class SecurityConfiguration(
    private val clientRegistrationRepository: ReactiveClientRegistrationRepository,
    private val applicationSecurityProperties: ApplicationSecurityProperties,
    private val problemSupport: SecurityProblemSupport,
    private val corsWebFilter: CorsWebFilter
) {
    @Value("\${spring.security.oauth2.client.provider.oidc.issuer-uri}")
    private lateinit var issuerUri: String

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        // @formatter:off
        return http {
            csrf {
                disable()
            }
            addFilterBefore(corsWebFilter, SecurityWebFiltersOrder.REACTOR_CONTEXT)
            headers {
                contentSecurityPolicy {
                    applicationSecurityProperties.contentSecurityPolicy
                }
                referrerPolicy { ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN }
                permissionsPolicy { PERMISSIONS_POLICY }
                frameOptions { mode = Mode.DENY }
            }
            exceptionHandling {
                accessDeniedHandler = problemSupport
                authenticationEntryPoint = problemSupport
            }
            authorizeExchange {
                authorize("/ping", permitAll)
                authorize("/swagger-ui.html", permitAll)
                authorize("/swagger-ui/**", permitAll)
                authorize("/v3/api-docs/**", permitAll)
                authorize("/hello", authenticated)
                authorize("/api/profile", authenticated)
                authorize("/api/profile/token", authenticated)
                authorize("/api/profile/role/admin", hasAnyRole("ADMIN"))
                authorize("/api/profile/scope/messages/read", hasAnyAuthority("ROLE_ADMIN"))
                authorize(anyExchange, authenticated)
            }
            oauth2Login {
                authorizationRequestResolver = this.clientRegistrationRepository?.let {
                    authorizationRequestResolver(
                        it
                    )
                }
            }
            oauth2ResourceServer {
                jwt {
                    jwtAuthenticationConverter = grantedAuthoritiesExtractor()
                }
            }
            oauth2Client { }
        }
        // @formatter:on
    }

    private fun grantedAuthoritiesExtractor(): Converter<Jwt, Mono<AbstractAuthenticationToken>> {
        val jwtAuthenticationConverter = JwtAuthenticationConverter()
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(GrantedAuthoritiesExtractor())
        return ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter)
    }

    private fun authorizationRequestResolver(clientRegistrationRepository: ReactiveClientRegistrationRepository): ServerOAuth2AuthorizationRequestResolver {
        val authorizationRequestResolver =
            DefaultServerOAuth2AuthorizationRequestResolver(clientRegistrationRepository)
        if (this.issuerUri.contains("auth0.com")) {
            authorizationRequestResolver.setAuthorizationRequestCustomizer(
                authorizationRequestCustomizer()
            )
        }
        return authorizationRequestResolver
    }

    private fun authorizationRequestCustomizer() =
        Consumer<OAuth2AuthorizationRequest.Builder> {
            it.authorizationRequestUri { uriBuilder ->
                uriBuilder.queryParam("audience", applicationSecurityProperties.oauth2.audience)
                    .build()
            }
        }
}
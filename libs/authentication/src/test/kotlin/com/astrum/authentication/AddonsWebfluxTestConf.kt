package com.astrum.authentication

import com.astrum.authentication.infrastructure.config.SpringAddonsSecurityProperties
import com.astrum.authentication.infrastructure.domain.OAuth2AuthoritiesConverter
import org.mockito.Mockito.mock
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Scope
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerReactiveAuthenticationManagerResolver
import org.springframework.security.oauth2.server.resource.introspection.ReactiveOpaqueTokenIntrospector
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.nio.charset.Charset
import java.security.Principal
import java.util.*


@TestConfiguration
@Import(WebTestClientProperties::class)
class AddonsWebfluxTestConf {
    @MockBean
    lateinit var jwtDecoder: ReactiveJwtDecoder

    @MockBean
    lateinit var jwtIssuerReactiveAuthenticationManagerResolver: JwtIssuerReactiveAuthenticationManagerResolver

    @MockBean
    lateinit var introspector: ReactiveOpaqueTokenIntrospector

    @Bean
    fun httpSecurity(): HttpSecurity {
        return mock(HttpSecurity::class.java)
    }

    @Bean
    @Scope("prototype")
    fun webTestClientSupport(
        webTestClientProperties: WebTestClientProperties,
        webTestClient: WebTestClient,
        securityProperties: SpringAddonsSecurityProperties
    ): WebTestClientSupport {
        return WebTestClientSupport(webTestClientProperties, webTestClient, securityProperties)
    }

    @ConditionalOnMissingBean
    @Bean
    fun authoritiesConverter(): OAuth2AuthoritiesConverter {
        return mock(OAuth2AuthoritiesConverter::class.java)
    }

    @ConditionalOnMissingBean
    @Bean
    fun serverAccessDeniedHandler(): ServerAccessDeniedHandler {
        return ServerAccessDeniedHandler { exchange: ServerWebExchange, ex: AccessDeniedException ->
            exchange.getPrincipal<Principal>().flatMap { principal: Principal ->
                val response =
                    exchange.response
                response.statusCode =
                    if (principal is AnonymousAuthenticationToken) HttpStatus.UNAUTHORIZED else HttpStatus.FORBIDDEN
                response.headers.contentType = MediaType.TEXT_PLAIN
                val dataBufferFactory = response.bufferFactory()
                val buffer =
                    dataBufferFactory.wrap(ex.message!!.toByteArray(Charset.defaultCharset()))
                response.writeWith(Mono.just(buffer))
                    .doOnError {
                        DataBufferUtils.release(
                            buffer
                        )
                    }
            }
        }
    }

    @ConditionalOnMissingBean
    @Bean
    @Throws(Exception::class)
    fun filterChain(
        http: ServerHttpSecurity,
        accessDeniedHandler: ServerAccessDeniedHandler,
        securityProperties: SpringAddonsSecurityProperties,
        serverProperties: ServerProperties
    ): SecurityWebFilterChain {
        if (securityProperties.permitAll.isNotEmpty()) {
            http.anonymous()
        }
        if (securityProperties.cors.isNotEmpty()) {
            http.cors().configurationSource(corsConfigurationSource(securityProperties))
        }
        val configurer = http.csrf()
        when (securityProperties.csrf) {
            SpringAddonsSecurityProperties.Csrf.DISABLE -> configurer.disable()
            SpringAddonsSecurityProperties.Csrf.DEFAULT -> if (securityProperties.statelessSessions) {
                configurer.disable()
            }

            SpringAddonsSecurityProperties.Csrf.SESSION -> {}
            SpringAddonsSecurityProperties.Csrf.COOKIE_HTTP_ONLY -> configurer.csrfTokenRepository(
                CookieServerCsrfTokenRepository()
            )

            SpringAddonsSecurityProperties.Csrf.COOKIE_ACCESSIBLE_FROM_JS -> configurer.csrfTokenRepository(
                CookieServerCsrfTokenRepository.withHttpOnlyFalse()
            )
        }
        if (securityProperties.statelessSessions) {
            http.securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
        }
        if (!securityProperties.redirectToLoginIfUnauthorizedOnRestrictedContent) {
            http.exceptionHandling().accessDeniedHandler(accessDeniedHandler)
        }
        if (serverProperties.ssl != null && serverProperties.ssl.isEnabled) {
            http.redirectToHttps()
        }
        if (securityProperties.statelessSessions) {
            http.securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
        }
        if (!securityProperties.redirectToLoginIfUnauthorizedOnRestrictedContent) {
            http.exceptionHandling().accessDeniedHandler(accessDeniedHandler)
        }
        if (serverProperties.ssl != null && serverProperties.ssl.isEnabled) {
            http.redirectToHttps()
        }
        http.authorizeExchange().pathMatchers(*securityProperties.permitAll).permitAll()
        return http.build()
    }

    private fun corsConfigurationSource(securityProperties: SpringAddonsSecurityProperties): CorsConfigurationSource {
        val source = UrlBasedCorsConfigurationSource()
        for (corsProps in securityProperties.cors) {
            val configuration = CorsConfiguration()
            configuration.allowedOrigins = corsProps.allowedOrigins.toList()
            configuration.allowedMethods = corsProps.allowedMethods.toList()
            configuration.allowedHeaders = corsProps.allowedHeaders.toList()
            configuration.exposedHeaders = corsProps.exposedHeaders.toList()
            source.registerCorsConfiguration(corsProps.path, configuration)
        }
        return source
    }
}

package com.astrum.authentication.infrastructure

import com.astrum.authentication.infrastructure.config.SpringAddonsSecurityProperties
import com.astrum.authentication.infrastructure.domain.AuthorizeExchangeSpecPostProcessor
import com.astrum.authentication.infrastructure.domain.OAuth2AuthenticationFactory
import com.astrum.authentication.infrastructure.domain.ServerHttpSecurityPostProcessor
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.convert.converter.Converter
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerReactiveAuthenticationManagerResolver
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.nio.charset.Charset
import java.security.Principal
import java.util.*
import java.util.stream.Stream


/**
 *
 *
 * **Usage**<br></br>
 * If not using spring-boot, &#64;Import or &#64;ComponentScan this class. All
 * beans defined here are &#64;ConditionalOnMissingBean =&gt;
 * just define your own &#64;Beans to override.
 *
 *
 *
 * **Provided &#64;Beans**
 *
 *
 *  * **SecurityWebFilterChain**: applies CORS, CSRF, anonymous,
 * sessionCreationPolicy, SSL redirect and 401 instead of redirect to
 * login properties as defined in [SpringAddonsSecurityProperties]
 *  * **AuthorizeExchangeSpecPostProcessor**. Override if you need fined
 * grained HTTP security (more than authenticated() to all routes
 * but the ones defined as permitAll() in
 * [SpringAddonsSecurityProperties]
 *  * **Jwt2AuthoritiesConverter**: responsible for converting the JWT into
 * Collection&lt;? extends GrantedAuthority&gt;
 *  * **ReactiveJwt2OpenidClaimSetConverter&lt;T extends Map&lt;String,
 * Object&gt; &amp; Serializable&gt;**: responsible for converting
 * the JWT into a claim-set of your choice (OpenID or not)
 *  * **ReactiveJwt2AuthenticationConverter&lt;OAuthentication&lt;T extends
 * OpenidClaimSet&gt;&gt;**: responsible for converting the JWT
 * into an Authentication (uses both beans above)
 *  * **ReactiveAuthenticationManagerResolver**: required to be able to
 * define more than one token issuer until
 * https://github.com/spring-projects/spring-boot/issues/30108 is solved
 *
 *
 * @author Yuniel Acosta
 */
@EnableWebFluxSecurity
@AutoConfiguration
@Import(AddonsSecurityBeans::class)
class AddonsWebSecurityBeans {
    /**
     *
     *
     * Applies SpringAddonsSecurityProperties to web security config. Be aware that
     * defining a [SecurityWebFilterChain] bean with no security matcher and
     * an order higher than LOWEST_PRECEDENCE will disable most of this lib
     * auto-configuration for OpenID resource-servers.
     *
     *
     *
     * You should consider to set security matcher to all other
     * [SecurityWebFilterChain] beans and provide
     * a [ServerHttpSecurityPostProcessor] bean to override anything from this
     * bean
     *
     * .
     *
     * @param http                          HTTP security to configure
     * @param serverProperties              Spring "server" configuration properties
     * @param addonsProperties              "com.astrum.springaddons.security"
     * configuration properties
     * @param authorizePostProcessor        Hook to override access-control rules
     * for all path that are not listed in
     * "permit-all"
     * @param httpPostProcessor             Hook to override all or part of
     * HttpSecurity auto-configuration
     * @param authenticationManagerResolver Converts successful JWT decoding result
     * into an [Authentication]
     * @param accessDeniedHandler           handler for unauthorized requests
     * (missing or invalid access-token)
     * @return A default [SecurityWebFilterChain] for reactive
     * resource-servers with JWT decoder(matches all unmatched routes with
     * lowest precedence)
     */
    @Order(Ordered.LOWEST_PRECEDENCE)
    @Bean
    fun resourceServerSecurityFilterChain(
        http: ServerHttpSecurity,
        serverProperties: ServerProperties,
        addonsProperties: SpringAddonsSecurityProperties,
        authorizePostProcessor: AuthorizeExchangeSpecPostProcessor,
        httpPostProcessor: ServerHttpSecurityPostProcessor,
        authenticationManagerResolver: ReactiveAuthenticationManagerResolver<ServerWebExchange>,
        accessDeniedHandler: ServerAccessDeniedHandler
    ): SecurityWebFilterChain {
        http.oauth2ResourceServer().authenticationManagerResolver(authenticationManagerResolver)
        if (addonsProperties.permitAll.isNotEmpty()) {
            http.anonymous()
        }
        if (addonsProperties.cors.isNotEmpty()) {
            http.cors().configurationSource(corsConfigurationSource(addonsProperties))
        } else {
            http.cors().disable()
        }
        when (addonsProperties.csrf) {
            SpringAddonsSecurityProperties.Csrf.DISABLE -> http.csrf().disable()
            SpringAddonsSecurityProperties.Csrf.DEFAULT -> if (addonsProperties.statelessSessions) {
                http.csrf().disable()
            } else {
                http.csrf()
            }

            SpringAddonsSecurityProperties.Csrf.SESSION -> {}
            SpringAddonsSecurityProperties.Csrf.COOKIE_HTTP_ONLY -> http.csrf()
                .csrfTokenRepository(CookieServerCsrfTokenRepository())

            SpringAddonsSecurityProperties.Csrf.COOKIE_ACCESSIBLE_FROM_JS -> http.csrf()
                .csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())
        }
        if (addonsProperties.statelessSessions) {
            http.securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
        }
        if (!addonsProperties.redirectToLoginIfUnauthorizedOnRestrictedContent) {
            http.exceptionHandling().accessDeniedHandler(accessDeniedHandler)
        }
        if (serverProperties.ssl != null && serverProperties.ssl.isEnabled) {
            http.redirectToHttps()
        }
        authorizePostProcessor.authorizeHttpRequests(
            http.authorizeExchange().pathMatchers(*addonsProperties.permitAll).permitAll()
        )
        return httpPostProcessor.process(http).build()
    }

    /**
     * Hook to override security rules for all path that are not listed in
     * "permit-all". Default is isAuthenticated().
     *
     * @return a hook to override security rules for all path that are not listed in
     * "permit-all". Default is isAuthenticated().
     */
    @ConditionalOnMissingBean
    @Bean
    fun authorizePostProcessor(): AuthorizeExchangeSpecPostProcessor {
        return object : AuthorizeExchangeSpecPostProcessor {
            override fun authorizeHttpRequests(spec: AuthorizeExchangeSpec): AuthorizeExchangeSpec {
                return spec.anyExchange().authenticated()
            }
        }
    }

    /**
     * Hook to override all or part of HttpSecurity auto-configuration.
     * Called after spring-addons configuration was applied so that you can
     * modify anything
     *
     * @return a hook to override all or part of HttpSecurity auto-configuration.
     * Called after spring-addons configuration was applied so that you can
     * modify anything
     */
    @ConditionalOnMissingBean
    @Bean
    fun httpPostProcessor(): ServerHttpSecurityPostProcessor {
        return object : ServerHttpSecurityPostProcessor {
            override fun process(serverHttpSecurity: ServerHttpSecurity): ServerHttpSecurity {
                return serverHttpSecurity
            }
        }
    }

    private fun corsConfigurationSource(securityProperties: SpringAddonsSecurityProperties): CorsConfigurationSource {
        log.debug(
            "Building default CorsConfigurationSource with: {}",
            Stream.of(securityProperties.cors).toList()
        )
        val source = UrlBasedCorsConfigurationSource()
        for (corsProps in securityProperties.cors) {
            val configuration = CorsConfiguration()
            configuration.allowedOrigins = corsProps.allowedOrigins
            configuration.allowedMethods = corsProps.allowedMethods
            configuration.allowedHeaders = corsProps.allowedHeaders
            configuration.exposedHeaders = corsProps.exposedHeaders
            source.registerCorsConfiguration(corsProps.path, configuration)
        }
        return source
    }

    interface Jwt2AuthenticationConverter : Converter<Jwt, Mono<AbstractAuthenticationToken>>

    /**
     * Converter bean from [Jwt] to [AbstractAuthenticationToken]
     *
     * @param authoritiesConverter  converts access-token claims into Spring
     * authorities
     * @param authenticationFactory builds an [Authentication] instance from
     * access-token string and claims
     * @return a converter from [Jwt] to [AbstractAuthenticationToken]
     */
    @ConditionalOnMissingBean
    @Bean
    fun jwtAuthenticationConverter(
        authoritiesConverter: Converter<Map<String, Any>, Collection<GrantedAuthority>>,
        authenticationFactory: Optional<OAuth2AuthenticationFactory>
    ): Jwt2AuthenticationConverter {
        return jwt2AuthenticationConverter(authenticationFactory, authoritiesConverter)
    }

    private fun jwt2AuthenticationConverter(
        authenticationFactory: Optional<OAuth2AuthenticationFactory>,
        authoritiesConverter: Converter<Map<String, Any>, Collection<GrantedAuthority>>
    ) = object : Jwt2AuthenticationConverter {
        override fun convert(source: Jwt): Mono<AbstractAuthenticationToken> {
            return authenticationFactory.map { af ->
                af.build(
                    source.tokenValue,
                    source.claims
                )
            }
                .orElse(
                    Mono.just(
                        JwtAuthenticationToken(
                            source,
                            authoritiesConverter.convert(source.claims)
                        )
                    )
                )
        }
    }

    /**
     * Provides with multi-tenancy: builds a ReactiveAuthenticationManagerResolver
     * per provided OIDC issuer URI
     *
     * @param auth2ResourceServerProperties "spring.security.oauth2.resourceserver"
     * configuration properties
     * @param addonsProperties              "com.astrum.springaddons.security"
     * configuration properties
     * @param jwtAuthenticationConverter    converts from a [Jwt] to an
     * [Authentication] implementation
     * @return Multi-tenant [ReactiveAuthenticationManagerResolver] (one for
     * each configured issuer)
     */
    @Suppress("UNCHECKED_CAST")
    @ConditionalOnMissingBean
    @Bean
    fun authenticationManagerResolver(
        auth2ResourceServerProperties: OAuth2ResourceServerProperties,
        addonsProperties: SpringAddonsSecurityProperties,
        jwtAuthenticationConverter: Converter<Jwt, Mono<AbstractAuthenticationToken>>
    ): ReactiveAuthenticationManagerResolver<ServerWebExchange> {
        val jwtProps = auth2ResourceServerProperties.jwt
        val jwtConf = jwtProps?.issuerUri ?: jwtProps?.jwkSetUri
        if (!jwtConf.isNullOrBlank()) {
            log.warn("spring.security.oauth2.resourceserver configuration will be ignored in favor of com.astrum.springaddons.security")
        }

        val jwtManagers: Map<SpringAddonsSecurityProperties.IssuerProperties, Mono<JwtReactiveAuthenticationManager>> =
            addonsProperties.issuers.associateWith { issuer ->
                val decoder =
                    if (issuer.jwkSetUri != null && issuer.jwkSetUri.toString().isNotBlank()) {
                        NimbusReactiveJwtDecoder.withJwkSetUri(issuer.jwkSetUri.toString()).build()
                    } else {
                        ReactiveJwtDecoders.fromIssuerLocation(issuer.location.toString())
                    }
                val provider = JwtReactiveAuthenticationManager(decoder)
                provider.setJwtAuthenticationConverter(jwtAuthenticationConverter)
                Mono.just(provider)
            }

        log.debug("Building default JwtIssuerReactiveAuthenticationManagerResolver with: $jwtProps ${addonsProperties.issuers}")
        return JwtIssuerReactiveAuthenticationManagerResolver(jwtManagers::get as ReactiveAuthenticationManagerResolver<String>)
    }


    /**
     * Bean to switch from default behavior of redirecting unauthorized
     * users to login (302) to returning 401 (unauthorized)
     *
     * @return a bean to switch from default behavior of redirecting unauthorized
     * users to login (302) to returning 401 (unauthorized)
     */
    @ConditionalOnMissingBean
    @Bean
    fun serverAccessDeniedHandler(): ServerAccessDeniedHandler {
        log.debug("Building default ServerAccessDeniedHandler")
        return ServerAccessDeniedHandler { exchange: ServerWebExchange, ex: AccessDeniedException ->
            exchange.getPrincipal<Principal>().flatMap { principal: Principal? ->
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
}

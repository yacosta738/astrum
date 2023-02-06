package com.astrum.authentication

import com.astrum.authentication.infrastructure.domain.AuthenticationBuilder
import org.springframework.http.client.reactive.ClientHttpConnector
import org.springframework.security.core.Authentication
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.web.reactive.server.MockServerConfigurer
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.MockServerSpec
import org.springframework.test.web.reactive.server.WebTestClientConfigurer
import org.springframework.web.server.adapter.WebHttpHandlerBuilder


/**
 * Redundant code for [Authentication] WebTestClient configurers
 *
 * @author     Yuniel Acosta
 * @param  <T> concrete [Authentication] type to build and configure in test security context
</T> */
interface AuthenticationConfigurer<T : Authentication> : WebTestClientConfigurer,
    MockServerConfigurer, AuthenticationBuilder<T> {
    override fun beforeServerCreated(builder: WebHttpHandlerBuilder) {
        (configurer() as MockServerConfigurer).beforeServerCreated(builder)
    }

    override fun afterConfigureAdded(serverSpec: MockServerSpec<*>) {
        (configurer() as MockServerConfigurer).afterConfigureAdded(serverSpec)
    }

    override fun afterConfigurerAdded(
        builder: WebTestClient.Builder,
        httpHandlerBuilder: WebHttpHandlerBuilder?,
        connector: ClientHttpConnector?
    ) {
        (configurer() as WebTestClientConfigurer).afterConfigurerAdded(
            builder,
            httpHandlerBuilder,
            connector
        )
    }

    fun <U> configurer(): U where U : WebTestClientConfigurer, U : MockServerConfigurer {
        return SecurityMockServerConfigurers.mockAuthentication(build())
    }
}

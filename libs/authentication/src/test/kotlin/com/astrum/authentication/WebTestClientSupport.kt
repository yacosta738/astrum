package com.astrum.authentication

import com.astrum.authentication.infrastructure.config.SpringAddonsSecurityProperties
import com.astrum.authentication.infrastructure.config.SpringAddonsSecurityProperties.Csrf
import org.springframework.http.MediaType
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec
import org.springframework.test.web.reactive.server.WebTestClientConfigurer
import java.nio.charset.Charset


class WebTestClientSupport(
    private val webTestClientProperties: WebTestClientProperties,
    private val webTestClient: WebTestClient,
    private val securityProperties: SpringAddonsSecurityProperties
) {
    private var mediaType: MediaType = MediaType.valueOf(webTestClientProperties.defaultMediaType)
    private var charset: Charset = Charset.forName(webTestClientProperties.defaultCharset)
    private var delegate: WebTestClient = webTestClient

    init {
        setCsrf(securityProperties.csrf != Csrf.DISABLE)
    }

    fun setMediaType(mediaType: MediaType): WebTestClientSupport {
        this.mediaType = mediaType
        return this
    }

    fun setCharset(charset: Charset): WebTestClientSupport {
        this.charset = charset
        return this
    }

    fun get(accept: MediaType, uriTemplate: String, vararg uriVars: Any): ResponseSpec =
        delegate.get().uri(uriTemplate, *uriVars).accept(accept).exchange()

    fun get(uriTemplate: String, vararg uriVars: Any): ResponseSpec =
        get(MediaType(mediaType, charset), uriTemplate, *uriVars)

    fun <T> post(
        payload: T,
        contentType: MediaType,
        charset: Charset,
        accept: MediaType,
        uriTemplate: String,
        vararg uriVars: Any
    ): ResponseSpec =
        delegate.post().uri(uriTemplate, *uriVars).accept(accept)
            .contentType(MediaType(contentType, charset)).bodyValue(payload as Any).exchange()

    fun <T> post(payload: T, uriTemplate: String, vararg uriVars: Any): ResponseSpec =
        post(payload, mediaType, charset, mediaType, uriTemplate, *uriVars)

    fun <T> put(
        payload: T,
        contentType: MediaType,
        charset: Charset,
        uriTemplate: String,
        vararg uriVars: Any
    ): ResponseSpec =
        delegate.put().uri(uriTemplate, *uriVars).contentType(MediaType(contentType, charset))
            .bodyValue(payload as Any).exchange()

    fun <T> put(payload: T, uriTemplate: String, vararg uriVars: Any): ResponseSpec {
        return put(payload, mediaType, charset, uriTemplate, uriVars)
    }

    fun <T> patch(
        payload: T,
        contentType: MediaType,
        charset: Charset,
        uriTemplate: String,
        vararg uriVars: Any
    ): ResponseSpec {
        return delegate.patch().uri(uriTemplate, *uriVars).contentType(
            MediaType(
                contentType, charset
            )
        ).bodyValue(payload as Any).exchange()
    }

    fun <T> patch(payload: T, uriTemplate: String, vararg uriVars: Any): ResponseSpec {
        return patch(payload, mediaType, charset, uriTemplate, *uriVars)
    }

    fun delete(uriTemplate: String, vararg uriVars: Any): ResponseSpec {
        return delegate.delete().uri(uriTemplate, *uriVars).exchange()
    }

    fun mutateWith(configurer: WebTestClientConfigurer): WebTestClientSupport {
        delegate = delegate.mutateWith(configurer)
        return this
    }

    fun setCsrf(isCsrf: Boolean): WebTestClientSupport {
        delegate.mutateWith(SecurityMockServerConfigurers.csrf())
        return this
    }
}

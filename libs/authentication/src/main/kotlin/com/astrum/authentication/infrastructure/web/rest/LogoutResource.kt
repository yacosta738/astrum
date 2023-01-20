package com.astrum.authentication.infrastructure.web.rest

import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.WebSession
import reactor.core.publisher.Mono

/**
 * REST controller for managing global OIDC logout.
 */
@RestController
class LogoutResource(
    private val registrations: ReactiveClientRegistrationRepository
) {
    private val registration = registrations.findByRegistrationId("oidc")

    /**
     * `POST  /api/logout` : logout the current user.
     *
     * @param idToken the ID token.
     * @param request a {@link ServerHttpRequest} request.
     * @param session the current {@link WebSession}.
     * @return the [ResponseEntity] with status `200 (OK)` and a body with a global logout URL.
     */
    @PostMapping("/api/logout")
    fun logout(
        @AuthenticationPrincipal(expression = "idToken") idToken: OidcIdToken,
        request: ServerHttpRequest,
        session: WebSession
    ): Mono<Map<String, String>> {
        return session.invalidate().then(
            registration.map {
                prepareLogoutUri(request, it, idToken)
            }
        )
    }

    private fun prepareLogoutUri(
        request: ServerHttpRequest,
        clientRegistration: ClientRegistration,
        idToken: OidcIdToken
    ): Map<String, String> {
        val logoutUrl = StringBuilder()
        val issuerUri = clientRegistration.providerDetails.issuerUri
        if (issuerUri.contains("auth0.com")) {
            val iUrl = if (issuerUri.endsWith("/")) {
                issuerUri + "v2/logout"
            } else {
                "$issuerUri/v2/logout"
            }
            logoutUrl.append(iUrl)
        } else {
            logoutUrl.append(clientRegistration.providerDetails.configurationMetadata["end_session_endpoint"].toString())
        }

        val originUrl = request.headers.origin
        if (issuerUri.contains("auth0.com")) {
            logoutUrl.append("?client_id=").append(clientRegistration.clientId).append("&returnTo=")
                .append(originUrl)
        } else {
            logoutUrl.append("?id_token_hint=").append(idToken.tokenValue)
                .append("&post_logout_redirect_uri=").append(originUrl)
        }
        return mapOf("logoutUrl" to logoutUrl.toString())
    }
}

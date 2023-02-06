package com.astrum.authentication.annotations

import com.astrum.authentication.infrastructure.OpenidClaimSet
import org.springframework.core.annotation.AliasFor
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.test.context.support.TestExecutionEvent
import org.springframework.security.test.context.support.WithSecurityContext
import java.lang.annotation.Inherited

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@MustBeDocumented
@WithSecurityContext(factory = WithOidcLogin.OAuth2AuthenticationTokenFactory::class)
annotation class WithOidcLogin(
    @get:AliasFor("authorities")
    val value: Array<String> = [],
    @get:AliasFor("value")
    val authorities: Array<String> = [],
    val claims: OpenIdClaims = OpenIdClaims(),
    val tokenString: String = "machin.truc.chose",
    val authorizedClientRegistrationId: String = "bidule",
    val nameAttributeKey: String = "sub",
    @get:AliasFor(annotation = WithSecurityContext::class, attribute = "setupBefore")
    val setupBefore: TestExecutionEvent = TestExecutionEvent.TEST_METHOD
) {
    class OAuth2AuthenticationTokenFactory(
        private val claims: OpenidClaimSet,
        private val authorities: Collection<GrantedAuthority>,
        private val authorizedClientRegistrationId: String
    ) : AbstractAnnotatedAuthenticationBuilder<WithOidcLogin, OAuth2AuthenticationToken>() {

        override fun authentication(annotation: WithOidcLogin): OAuth2AuthenticationToken {
            val principal = DefaultOidcUser(
                authorities,
                OidcIdToken(annotation.tokenString, claims.issuedAt, claims.expiresAt, claims)
            )
            return OAuth2AuthenticationToken(principal, authorities, authorizedClientRegistrationId)
        }
    }
}

package com.astrum.authentication.annotations

import com.astrum.authentication.infrastructure.OpenidClaimSet
import org.springframework.core.annotation.AliasFor
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.test.context.support.TestExecutionEvent
import org.springframework.security.test.context.support.WithSecurityContext
import java.lang.annotation.Inherited

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@MustBeDocumented
@WithSecurityContext(factory = WithOAuth2Login.OAuth2AuthenticationTokenFactory::class)
annotation class WithOAuth2Login(
    @get:AliasFor("authorities")
    val value: Array<String> = [],
    @get:AliasFor("value")
    val authorities: Array<String> = [],
    val claims: OpenIdClaims = OpenIdClaims(),
    val tokenString: String = "machin.truc.chose",
    val authorizedClientRegistrationId: String = "bidule",
    val nameAttributeKey: String = "sub",
    @get:AliasFor(annotation = WithSecurityContext::class)
    val setupBefore: TestExecutionEvent = TestExecutionEvent.TEST_METHOD
) {
    class OAuth2AuthenticationTokenFactory :
        AbstractAnnotatedAuthenticationBuilder<WithOAuth2Login, OAuth2AuthenticationToken>() {
        override fun authentication(annotation: WithOAuth2Login): OAuth2AuthenticationToken {
            val token = OpenidClaimSet(super.claims(annotation.claims))
            val authorities = super.authorities(*annotation.authorities)
            val principal = DefaultOAuth2User(authorities, token, annotation.nameAttributeKey)
            return OAuth2AuthenticationToken(
                principal,
                authorities,
                annotation.authorizedClientRegistrationId
            )
        }
    }
}

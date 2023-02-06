package com.astrum.authentication.annotations

import org.springframework.core.annotation.AliasFor
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.jwt.JwtClaimNames
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal
import org.springframework.security.test.context.support.TestExecutionEvent
import org.springframework.security.test.context.support.WithSecurityContext
import java.lang.annotation.Inherited

/**
 * Annotation to setup test {@link SecurityContext} with an {@link BearerTokenAuthentication}. Sample usage:
 *
 * <pre>
 * &#64;Test
 * &#64;WithMockOidcId(
authorities = { "USER", "AUTHORIZED_PERSONNEL" },
claims = &#64;OpenIdClaims(
sub = "42",
email = "ch4mp@c4-soft.com",
emailVerified = true,
nickName = "Tonton-Pirate",
preferredUsername = "ch4mpy",
otherClaims = &#64;ClaimSet(stringClaims = &#64;StringClaim(name = "foo", value = "bar"))))
 * public void test() {
 *     ...
 * }
 * </pre>
 *
 * @author Yuniel Acosta
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@MustBeDocumented
@WithSecurityContext(factory = WithMockBearerTokenAuthentication.AuthenticationFactory::class)
annotation class WithMockBearerTokenAuthentication(
    @get:AliasFor("authorities")
    val value: Array<String> = [],
    @get:AliasFor("value")
    val authorities: Array<String> = [],
    val attributes: OpenIdClaims = OpenIdClaims(),
    val bearerString: String = "machin.truc.chose",
    /**
     * Determines when the {@link SecurityContext} is setup. The default is before {@link TestExecutionEvent#TEST_METHOD} which occurs during
     * {@link org.springframework.test.context.TestExecutionListener#beforeTestMethod(TestContext)}
     *
     * @return the {@link TestExecutionEvent} to initialize before
     */
    @get:AliasFor(annotation = WithSecurityContext::class)
    val setupBefore: TestExecutionEvent = TestExecutionEvent.TEST_METHOD
) {
    class AuthenticationFactory :
        AbstractAnnotatedAuthenticationBuilder<WithMockBearerTokenAuthentication, BearerTokenAuthentication>() {
        override fun authentication(annotation: WithMockBearerTokenAuthentication): BearerTokenAuthentication {
            val claims = super.claims(annotation.attributes)
            val authorities = super.authorities(*annotation.authorities)
            val principal = OAuth2IntrospectionAuthenticatedPrincipal(claims, authorities)
            val credentials = OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                annotation.bearerString,
                claims.getAsInstant(JwtClaimNames.IAT),
                claims.getAsInstant(JwtClaimNames.EXP)
            )
            return BearerTokenAuthentication(principal, credentials, authorities)
        }
    }
}

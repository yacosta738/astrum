package com.astrum.authentication.annotations

import com.astrum.authentication.infrastructure.OAuthentication
import com.astrum.authentication.infrastructure.OpenidClaimSet
import org.springframework.core.annotation.AliasFor
import org.springframework.security.test.context.support.TestExecutionEvent
import org.springframework.security.test.context.support.WithSecurityContext
import java.lang.annotation.Inherited


/**
 * Annotation to setup test [SecurityContext] with an [OAuthentication]. Sample usage:
 *
 * <pre>
 * &#64;Test
 * &#64;WithMockOidcId(
 * authorities = { "USER", "AUTHORIZED_PERSONNEL" },
 * claims = &#64;OpenIdClaims(
 * sub = "42",
 * email = "ch4mp@c4-soft.com",
 * emailVerified = true,
 * nickName = "Tonton-Pirate",
 * preferredUsername = "ch4mpy",
 * otherClaims = &#64;ClaimSet(stringClaims = &#64;StringClaim(name = "foo", value = "bar"))))
 * public void test() {
 * ...
 * }
</pre> *
 *
 * @author Yuniel Acosta
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@MustBeDocumented
@WithSecurityContext(factory = OpenId.AuthenticationFactory::class)
annotation class OpenId(
    @get:AliasFor("authorities") vararg val value: String = [],
    @get:AliasFor("value") val authorities: Array<String> = [],
    val claims: OpenIdClaims = OpenIdClaims(),
    val bearerString: String = "machin.truc.chose",
    @get:AliasFor(annotation = WithSecurityContext::class) val setupBefore: TestExecutionEvent = TestExecutionEvent.TEST_METHOD
) {
    object AuthenticationFactory :
        AbstractAnnotatedAuthenticationBuilder<OpenId, OAuthentication<OpenidClaimSet>>() {
        override fun authentication(annotation: OpenId): OAuthentication<OpenidClaimSet> {
            val claims: Map<String, Any> = super.claims(annotation.claims)
            return OAuthentication(
                OpenidClaimSet(claims),
                super.authorities(*annotation.authorities),
                annotation.bearerString
            )
        }

    }
}

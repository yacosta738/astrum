package com.astrum.authentication.annotations

import com.astrum.authentication.infrastructure.OpenidClaimSet
import org.springframework.core.annotation.AliasFor
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.test.context.support.TestExecutionEvent
import org.springframework.security.test.context.support.WithSecurityContext
import java.lang.annotation.Inherited

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Inherited
@WithSecurityContext(factory = WithMockJwtAuth.JwtAuthenticationTokenFactory::class)
annotation class WithMockJwtAuth(
    @get:AliasFor("authorities") val value: Array<String> = [],
    @get:AliasFor("value") val authorities: Array<String> = [],
    val claims: OpenIdClaims = OpenIdClaims(),
    val tokenString: String = "machin.truc.chose",
    val headers: Claims = Claims(stringClaims = [StringClaim(name = "alg", value = "none")]),
    @get:AliasFor(annotation = WithSecurityContext::class) val setupBefore: TestExecutionEvent = TestExecutionEvent.TEST_METHOD
) {
    class JwtAuthenticationTokenFactory :
        AbstractAnnotatedAuthenticationBuilder<WithMockJwtAuth, JwtAuthenticationToken>() {
        override fun authentication(annotation: WithMockJwtAuth): JwtAuthenticationToken {
            val token = OpenidClaimSet(super.claims(annotation.claims))
            val jwt = Jwt(
                annotation.tokenString,
                token.issuedAt,
                token.expiresAt,
                Claims.of(annotation.headers),
                token
            )
            return JwtAuthenticationToken(jwt, super.authorities(*annotation.authorities))
        }
    }
}

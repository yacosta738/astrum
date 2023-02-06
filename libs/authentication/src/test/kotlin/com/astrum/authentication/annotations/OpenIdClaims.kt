package com.astrum.authentication.annotations

import com.astrum.authentication.Defaults
import com.astrum.authentication.OpenidClaimSetBuilder
import java.net.MalformedURLException
import java.net.URL
import java.time.Instant
import java.util.*


/**
 * Configures claims defined at
 * [https://datatracker.ietf.org/doc/html/rfc7519#section-4.1](https://datatracker.ietf.org/doc/html/rfc7519#section-4.1) and
 * [https://openid.net/specs/openid-connect-core-1_0.html#IDToken](https://openid.net/specs/openid-connect-core-1_0.html#IDToken)
 *
 * @author Yuniel Acosta
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class OpenIdClaims(
    val acr: String = "",
    val amr: Array<String> = [],
    val aud: Array<String> = [],
    val azp: String = "",
    val authTime: String = "",
    val exp: String = "",
    val iat: String = "",
    val jti: String = "",
    val nbf: String = "",
    val iss: String = "",
    val nonce: String = "",
    val sub: String = Defaults.SUBJECT,
    val sessionState: String = "",
    val accessTokenHash: String = "",
    val authorizationCodeHash: String = "",
    val address: OpenIdAddress = OpenIdAddress(),
    val birthdate: String = "",
    val email: String = "",
    val emailVerified: Boolean = false,
    val familyName: String = "",
    val gender: String = "",
    val givenName: String = "",
    val locale: String = "",
    val middleName: String = "",
    val name: String = "",
    val nickName: String = "",
    val phoneNumber: String = "",
    val phoneNumberVerified: Boolean = false,
    val picture: String = "",
    val preferredUsername: String = Defaults.AUTH_NAME,
    val profile: String = "",
    val updatedAt: String = "",
    val website: String = "",
    val zoneinfo: String = "",
    val otherClaims: Claims = Claims()
) {
    class Builder private constructor() {
        companion object {
            fun of(tokenAnnotation: OpenIdClaims): OpenidClaimSetBuilder {
                val token = OpenidClaimSetBuilder(Claims.of(tokenAnnotation.otherClaims))
                if (tokenAnnotation.iss.isNotBlank()) {
                    try {
                        token.issuer(URL(tokenAnnotation.iss))
                    } catch (e: MalformedURLException) {
                        throw InvalidClaimException(e)
                    }
                }
                if (tokenAnnotation.exp.isNotBlank()) {
                    token.expiresAt(Instant.parse(tokenAnnotation.exp))
                }
                if (tokenAnnotation.iat.isNotBlank()) {
                    token.issuedAt(Instant.parse(tokenAnnotation.iat))
                }
                if (tokenAnnotation.authTime.isNotBlank()) {
                    token.authTime(Instant.parse(tokenAnnotation.authTime))
                }
                if (tokenAnnotation.sessionState.isNotBlank()) {
                    token.sessionState(tokenAnnotation.sessionState)
                    token.accessTokenHash(tokenAnnotation.accessTokenHash)
                    token.authorizationCodeHash(tokenAnnotation.authorizationCodeHash)
                }
                token
                    .subject(tokenAnnotation.sub)
                    .audience(tokenAnnotation.aud.toList())
                    .nonce(tokenAnnotation.nonce)
                    .acr(tokenAnnotation.acr)
                    .amr(tokenAnnotation.amr.toList())
                    .azp(tokenAnnotation.azp)
                if (tokenAnnotation.updatedAt.isNotBlank()) {
                    token.updatedAt(Instant.parse(tokenAnnotation.updatedAt))
                }
                return token
                    .address(OpenIdAddress.of(tokenAnnotation.address))
                    .birthdate(blankIfEmpty(tokenAnnotation.birthdate))
                    .email(blankIfEmpty(tokenAnnotation.email))
                    .emailVerified(tokenAnnotation.emailVerified)
                    .familyName(blankIfEmpty(tokenAnnotation.familyName))
                    .gender(blankIfEmpty(tokenAnnotation.gender))
                    .givenName(blankIfEmpty(tokenAnnotation.givenName))
                    .jwtId(tokenAnnotation.jti)
                    .locale(blankIfEmpty(tokenAnnotation.locale))
                    .middleName(blankIfEmpty(tokenAnnotation.middleName))
                    .name(blankIfEmpty(tokenAnnotation.name))
                    .nickname(blankIfEmpty(tokenAnnotation.nickName))
                    .phoneNumber(blankIfEmpty(tokenAnnotation.phoneNumber))
                    .phoneNumberVerified(tokenAnnotation.phoneNumberVerified)
                    .preferredUsername(blankIfEmpty(tokenAnnotation.preferredUsername))
                    .picture(blankIfEmpty(tokenAnnotation.picture))
                    .profile(blankIfEmpty(tokenAnnotation.profile))
                    .website(blankIfEmpty(tokenAnnotation.website))
            }

            private fun blankIfEmpty(value: String): String {
                return value.ifBlank { "" }
            }
        }
    }
}

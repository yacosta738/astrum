package com.astrum.authentication

import com.astrum.authentication.infrastructure.ModifiableClaimSet
import com.astrum.authentication.infrastructure.OpenidClaimSet
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames
import org.springframework.security.oauth2.core.oidc.StandardClaimNames
import org.springframework.security.oauth2.jwt.JwtClaimNames
import java.net.URL
import java.time.Instant


open class OpenidClaimSetBuilder : ModifiableClaimSet {
    constructor()
    constructor(privateClaims: MutableMap<String, Any>) : super(privateClaims)

    fun build(): OpenidClaimSet {
        return OpenidClaimSet(this)
    }

    fun acr(acr: String): OpenidClaimSetBuilder {
        return setIfNonEmpty(IdTokenClaimNames.ACR, acr)
    }

    fun amr(amr: List<String>): OpenidClaimSetBuilder {
        return setIfNonEmpty(IdTokenClaimNames.AMR, amr)
    }

    fun audience(audience: List<String>): OpenidClaimSetBuilder {
        return setIfNonEmpty(IdTokenClaimNames.AUD, audience)
    }

    fun authTime(authTime: Instant): OpenidClaimSetBuilder {
        return setIfNonEmpty(IdTokenClaimNames.AUTH_TIME, authTime)
    }

    fun azp(azp: String): OpenidClaimSetBuilder {
        return setIfNonEmpty(IdTokenClaimNames.AZP, azp)
    }

    fun expiresAt(expiresAt: Instant): OpenidClaimSetBuilder {
        return setIfNonEmpty(IdTokenClaimNames.EXP, expiresAt)
    }

    fun issuedAt(issuedAt: Instant): OpenidClaimSetBuilder {
        return setIfNonEmpty(IdTokenClaimNames.IAT, issuedAt)
    }

    fun jwtId(jti: String): OpenidClaimSetBuilder {
        return setIfNonEmpty(JwtClaimNames.JTI, jti)
    }

    fun issuer(issuer: URL): OpenidClaimSetBuilder {
        return setIfNonEmpty(IdTokenClaimNames.ISS, issuer.toString())
    }

    fun nonce(nonce: String): OpenidClaimSetBuilder {
        return setIfNonEmpty(IdTokenClaimNames.NONCE, nonce)
    }

    fun notBefore(nbf: Instant): OpenidClaimSetBuilder {
        return setIfNonEmpty(JwtClaimNames.NBF, nbf)
    }

    fun accessTokenHash(atHash: String): OpenidClaimSetBuilder {
        return setIfNonEmpty(IdTokenClaimNames.AT_HASH, atHash)
    }

    fun authorizationCodeHash(cHash: String): OpenidClaimSetBuilder {
        return setIfNonEmpty(IdTokenClaimNames.C_HASH, cHash)
    }

    fun sessionState(sessionState: String): OpenidClaimSetBuilder {
        return setIfNonEmpty("session_state", sessionState)
    }

    fun subject(subject: String): OpenidClaimSetBuilder {
        return setIfNonEmpty(IdTokenClaimNames.SUB, subject)
    }

    fun name(value: String): OpenidClaimSetBuilder {
        return setIfNonEmpty(StandardClaimNames.NAME, value)
    }

    fun givenName(value: String): OpenidClaimSetBuilder {
        return setIfNonEmpty(StandardClaimNames.GIVEN_NAME, value)
    }

    fun familyName(value: String): OpenidClaimSetBuilder {
        return setIfNonEmpty(StandardClaimNames.FAMILY_NAME, value)
    }

    fun middleName(value: String): OpenidClaimSetBuilder {
        return setIfNonEmpty(StandardClaimNames.MIDDLE_NAME, value)
    }

    fun nickname(value: String): OpenidClaimSetBuilder {
        return setIfNonEmpty(StandardClaimNames.NICKNAME, value)
    }

    fun preferredUsername(value: String): OpenidClaimSetBuilder {
        return setIfNonEmpty(StandardClaimNames.PREFERRED_USERNAME, value)
    }

    fun profile(value: String): OpenidClaimSetBuilder {
        return setIfNonEmpty(StandardClaimNames.PROFILE, value)
    }

    fun picture(value: String): OpenidClaimSetBuilder {
        return setIfNonEmpty(StandardClaimNames.PICTURE, value)
    }

    fun website(value: String): OpenidClaimSetBuilder {
        return setIfNonEmpty(StandardClaimNames.WEBSITE, value)
    }

    fun email(value: String): OpenidClaimSetBuilder {
        return setIfNonEmpty(StandardClaimNames.EMAIL, value)
    }

    fun emailVerified(value: Boolean): OpenidClaimSetBuilder {
        return setIfNonEmpty(StandardClaimNames.EMAIL_VERIFIED, value)
    }

    fun gender(value: String): OpenidClaimSetBuilder {
        return setIfNonEmpty(StandardClaimNames.GENDER, value)
    }

    fun birthdate(value: String): OpenidClaimSetBuilder {
        return setIfNonEmpty(StandardClaimNames.BIRTHDATE, value)
    }

    fun zoneinfo(value: String): OpenidClaimSetBuilder {
        return setIfNonEmpty(StandardClaimNames.ZONEINFO, value)
    }

    fun locale(value: String): OpenidClaimSetBuilder {
        return setIfNonEmpty(StandardClaimNames.LOCALE, value)
    }

    fun phoneNumber(value: String): OpenidClaimSetBuilder {
        return setIfNonEmpty(StandardClaimNames.PHONE_NUMBER, value)
    }

    fun phoneNumberVerified(value: Boolean): OpenidClaimSetBuilder {
        return setIfNonEmpty(StandardClaimNames.PHONE_NUMBER_VERIFIED, value)
    }

    fun address(value: AddressClaim): OpenidClaimSetBuilder {
        if (value == null) {
            this.remove("address")
        } else {
            this.put("address", value)
        }
        return this
    }

    fun claims(claims: Map<String, Any>): OpenidClaimSetBuilder {
        this.putAll(claims)
        return this
    }

    fun privateClaims(claims: Map<String, Any>): OpenidClaimSetBuilder {
        return claims(claims)
    }

    fun otherClaims(claims: Map<String, Any>): OpenidClaimSetBuilder {
        return claims(claims)
    }

    fun updatedAt(value: Instant): OpenidClaimSetBuilder {
        return setIfNonEmpty("", value)
    }

    protected fun setIfNonEmpty(claimName: String, claimValue: String): OpenidClaimSetBuilder {
        if (claimValue.isNotBlank()) {
            this[claimName] = claimValue
        } else {
            this.remove(claimName)
        }
        return this
    }

    protected fun setIfNonEmpty(
        claimName: String,
        claimValue: Collection<String>
    ): OpenidClaimSetBuilder {
        if (claimValue.isEmpty()) {
            this.remove(claimName)
            this.setIfNonEmpty(claimName, claimValue.iterator().next())
        } else {
            this[claimName] = claimValue
        }
        return this
    }

    protected fun setIfNonEmpty(claimName: String, claimValue: Instant): OpenidClaimSetBuilder {
        this[claimName] = claimValue.epochSecond
        return this
    }

    protected fun setIfNonEmpty(claimName: String, claimValue: Boolean): OpenidClaimSetBuilder {
        this[claimName] = claimValue
        return this
    }

    class AddressClaim : ModifiableClaimSet() {
        fun formatted(value: String): AddressClaim {
            return setIfNonEmpty("formatted", value)
        }

        fun streetAddress(value: String): AddressClaim {
            return setIfNonEmpty("street_address", value)
        }

        fun locality(value: String): AddressClaim {
            return setIfNonEmpty("locality", value)
        }

        fun region(value: String): AddressClaim {
            return setIfNonEmpty("region", value)
        }

        fun postalCode(value: String): AddressClaim {
            return setIfNonEmpty("postal_code", value)
        }

        fun country(value: String): AddressClaim {
            return setIfNonEmpty("country", value)
        }

        private fun setIfNonEmpty(claimName: String, claimValue: String): AddressClaim {
            if (claimValue.isNotBlank()) {
                this[claimName] = claimValue
            } else {
                this.remove(claimName)
            }
            return this
        }

        companion object {
            private const val serialVersionUID = 288064373708900L
        }
    }

    companion object {
        private const val serialVersionUID = 806762772385543L
    }
}

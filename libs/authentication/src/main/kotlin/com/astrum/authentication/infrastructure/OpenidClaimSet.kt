package com.astrum.authentication.infrastructure

import org.springframework.security.oauth2.core.oidc.IdTokenClaimAccessor
import java.security.Principal


class OpenidClaimSet(claims: Map<String, Any>) : UnmodifiableClaimSet(claims), IdTokenClaimAccessor,
    Principal {
    override fun getClaims(): Map<String, Any> {
        return this
    }

    override fun getName(): String {
        return subject
    }

    companion object {
        private const val serialVersionUID = -51412122993529528L
    }
}

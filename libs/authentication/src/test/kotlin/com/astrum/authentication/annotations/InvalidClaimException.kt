package com.astrum.authentication.annotations

class InvalidClaimException(t: Throwable) : RuntimeException("Invalid Claim", t) {
    companion object {
        private const val serialVersionUID = -2603521800687945747L
    }
}

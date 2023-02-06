package com.astrum.authentication.infrastructure.exceptions

sealed class AuthenticationException : RuntimeException {
    companion object {
        private const val serialVersionUID = 123456789L
    }

    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}

class MissingAuthorizationServerConfigurationException(jwtIssuer: String) : AuthenticationException(
    String.format(
        "Missing authorities mapping configuration for issuer: %s",
        jwtIssuer
    )
)

class UnparsableClaimException(message: String) : RuntimeException(message) {
    companion object {
        private const val serialVersionUID = 5585678138757632513L
    }
}

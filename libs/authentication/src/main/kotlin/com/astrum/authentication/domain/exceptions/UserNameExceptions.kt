package com.astrum.authentication.domain.exceptions

sealed class InvalidArgumentUserNameException(
    override val message: String,
    override val cause: Throwable? = null
) : IllegalArgumentException(message, cause)

data class UserNameNotValidException(val id: String, override val cause: Throwable? = null) :
    InvalidArgumentUserNameException("The username <$id> is not valid", cause)

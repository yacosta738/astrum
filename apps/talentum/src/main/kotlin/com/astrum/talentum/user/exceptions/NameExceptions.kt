package com.astrum.talentum.user.exceptions

sealed class InvalidArgumentNameException(
    override val message: String,
    override val cause: Throwable? = null
) : IllegalArgumentException(message, cause)

data class FirstNameNotValidException(val id: String, override val cause: Throwable? = null) :
    InvalidArgumentNameException("The first name <$id> is not valid", cause)

data class LastNameNotValidException(val id: String, override val cause: Throwable? = null) :
    InvalidArgumentNameException("The last name <$id> is not valid", cause)

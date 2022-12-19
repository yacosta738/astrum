package com.astrum.talentum.user.exceptions

sealed class InvalidArgumentEmailException(
    override val message: String,
    override val cause: Throwable? = null
) : IllegalArgumentException(message, cause)

data class EmailNotValidException(val id: String, override val cause: Throwable? = null) :
    InvalidArgumentEmailException("The email <$id> is not valid", cause)

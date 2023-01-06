package com.astrum.authentication.domain

import com.astrum.authentication.domain.exceptions.UserNameNotValidException
import com.astrum.common.domain.BaseValidateValueObject
import com.astrum.common.domain.BaseValueObject
import java.util.*

private const val MIN_LENGTH = 3
private const val MAX_LENGTH = 100

data class Username(val username: String) : BaseValidateValueObject<String>(username) {
    override fun validate(value: String) {
        require(value.length in MIN_LENGTH..MAX_LENGTH) {
            throw UserNameNotValidException(value)
        }
    }

    /**
     * Compares this object with the specified object for order. Returns zero if this object is equal
     * to the specified [other] object, a negative number if it's less than [other], or a positive number
     * if it's greater than [other].
     */
    override fun compareTo(other: BaseValueObject<String>): Int {
        TODO("Not yet implemented")
    }

    companion object {
        fun of(username: String): Optional<Username> =
            Optional.ofNullable(username).filter(String::isNotBlank).map(::Username)
    }
}
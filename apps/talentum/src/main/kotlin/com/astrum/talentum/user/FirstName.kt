package com.astrum.talentum.user

import com.astrum.common.domain.BaseValidateValueObject
import com.astrum.common.domain.BaseValueObject
import com.astrum.talentum.user.exceptions.FirstNameNotValidException

private const val NAME_LEN = 150

/**
 * Email value object
 * @param firstname first name value
 * @throws FirstNameNotValidException if first name is not valid
 * @see BaseValidateValueObject
 * @see BaseValueObject
 * @see FirstNameNotValidException
 * @author Yuniel Acosta
 */
data class FirstName(val firstname: String) : BaseValidateValueObject<String>(firstname) {
    companion object {
        private const val serialVersionUID = 1L
    }

    /**
     * Validate first name value object with regex
     * @param value first name value
     * @throws FirstNameNotValidException if first name is not valid
     */
    override fun validate(value: String) {
        val firstname = value.trim()
        if (firstname.isEmpty() || firstname.length > NAME_LEN || containsInvalidCharacters(
                firstname
            )
        ) {
            throw FirstNameNotValidException(value)
        }
    }

    private fun containsInvalidCharacters(firstname: String): Boolean {
        return !firstname.matches(Regex("^[a-zA-ZÀ-ÿ\\s]{1,150}\$"))
    }

    /**
     * Compares this object with the specified object for order. Returns zero if this object is equal
     * to the specified [other] object, a negative number if it's less than [other], or a positive number
     * if it's greater than [other].
     */
    override fun compareTo(other: BaseValueObject<String>): Int {
        return firstname.compareTo(other.value)
    }

    override fun toString(): String {
        return firstname
    }

}
package com.astrum.talentum.user

import com.astrum.common.domain.BaseValidateValueObject
import com.astrum.common.domain.BaseValueObject
import com.astrum.talentum.user.exceptions.LastNameNotValidException

private const val NAME_LEN = 150

/**
 * Email value object
 * @param lastname last name value
 * @throws LastNameNotValidException if last name is not valid
 * @see BaseValidateValueObject
 * @see BaseValueObject
 * @see LastNameNotValidException
 * @author Yuniel Acosta
 */
data class LastName(val lastname: String) : BaseValidateValueObject<String>(lastname) {
    companion object {
        private const val serialVersionUID = 1L

    }

    /**
     * Validate last name value object
     * @param value last name value
     * @throws LastNameNotValidException if last name is not valid
     */
    override fun validate(value: String) {
        val lastname = value.trim()
        if (lastname.isEmpty() || lastname.length > NAME_LEN) {
            throw LastNameNotValidException(value)
        }
    }

    /**
     * Compares this object with the specified object for order. Returns zero if this object is equal
     * to the specified [other] object, a negative number if it's less than [other], or a positive number
     * if it's greater than [other].
     */
    override fun compareTo(other: BaseValueObject<String>): Int {
        return lastname.compareTo(other.value)
    }

    override fun toString(): String {
        return lastname
    }

}
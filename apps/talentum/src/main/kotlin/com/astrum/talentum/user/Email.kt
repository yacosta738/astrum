package com.astrum.talentum.user

import com.astrum.common.domain.BaseValidateValueObject
import com.astrum.common.domain.BaseValueObject
import com.astrum.talentum.user.exceptions.EmailNotValidException

private const val EMAIL_LEN = 255

/**
 * Email value object
 * @param email value
 * @throws EmailNotValidException if email is not valid
 * @see BaseValidateValueObject
 * @see BaseValueObject
 * @see EmailNotValidException
 * @author Yuniel Acosta
 */
data class Email(val email: String) : BaseValidateValueObject<String>(email) {
    companion object {
        private const val serialVersionUID = 1L
        private const val regex =
            "^[a-zA-Z0-9.!#\$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*\$"
    }

    /**
     * Validate email value object with regex
     * @param value email value
     * @throws EmailNotValidException if email is not valid
     */
    override fun validate(value: String) {
        if (value.length > EMAIL_LEN || !value.matches(regex.toRegex())) {
            throw EmailNotValidException(value)
        }
    }

    /**
     * Compares this object with the specified object for order. Returns zero if this object is equal
     * to the specified [other] object, a negative number if it's less than [other], or a positive number
     * if it's greater than [other].
     */
    override fun compareTo(other: BaseValueObject<String>): Int {
        return email.compareTo(other.value)
    }

}
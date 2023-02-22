package com.astrum.common.domain

import java.io.Serializable


abstract class BaseValueObject<T> protected constructor(val value: T) :
    Comparable<BaseValueObject<T>>, Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BaseValueObject<*>) return false

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value?.hashCode() ?: 0
    }

    override fun toString(): String {
        return value.toString()
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}

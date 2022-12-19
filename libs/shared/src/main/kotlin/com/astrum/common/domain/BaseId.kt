package com.astrum.common.domain


abstract class BaseId<T> protected constructor(private val value: T) {
    init {
        require(value != null) { "The id cannot be null" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BaseId<*>) return false

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value?.hashCode() ?: 0
    }
}
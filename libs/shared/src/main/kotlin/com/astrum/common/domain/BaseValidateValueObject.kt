package com.astrum.common.domain

abstract class BaseValidateValueObject<T> protected constructor(value: T) :
    BaseValueObject<T>(value) {
    init {
        this.validate(value)
    }

    abstract fun validate(value: T)
}
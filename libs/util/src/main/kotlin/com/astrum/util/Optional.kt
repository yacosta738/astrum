package com.astrum.util

import java.util.*

inline fun <T> resolveNotNull(optional: Optional<T>?, value: () -> T): T {
    return resolve(optional, value) ?: throw RuntimeException("it must be present.")
}

inline fun <T> resolve(optional: Optional<T>?, value: () -> T?): T? {
    if (optional == null) {
        return value()
    }
    if (optional.isPresent) {
        return optional.get()
    }

    return null
}

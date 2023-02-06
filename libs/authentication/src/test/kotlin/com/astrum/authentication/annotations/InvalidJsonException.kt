package com.astrum.authentication.annotations

class InvalidJsonException(t: Throwable) : RuntimeException("Invalid parse json ", t) {
    companion object {
        private const val serialVersionUID = -2552357765648L
    }
}

package com.astrum.authentication

class ConversionFailedException(message: String) : RuntimeException(message) {
    companion object {
        private const val serialVersionUID = 8295304756267987916L
    }
}

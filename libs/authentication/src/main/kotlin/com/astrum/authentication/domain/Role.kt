package com.astrum.authentication.domain

enum class Role {
    ADMIN,
    USER,
    ANONYMOUS,
    UNKNOWN;

    fun key(): String {
        return PREFIX + name
    }

    companion object {
        private const val PREFIX = "ROLE_"
        fun from(key: String): Role = values().find { it.key() == key } ?: UNKNOWN
    }
}
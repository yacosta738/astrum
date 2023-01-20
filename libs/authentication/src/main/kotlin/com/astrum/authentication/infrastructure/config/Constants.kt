package com.astrum.authentication.infrastructure.config

// Regex for acceptable logins
const val LOGIN_REGEX: String =
    "^(?>[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*)|(?>[_.@A-Za-z0-9-]+)$"
const val SYSTEM_ACCOUNT: String = "system"
const val DEFAULT_LANGUAGE: String = "en"

const val ADMIN: String = "ROLE_ADMIN"
const val USER: String = "ROLE_USER"
const val ANONYMOUS: String = "ROLE_ANONYMOUS"

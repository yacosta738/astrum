package com.astrum.authentication.infrastructure.domain

import org.springframework.security.config.web.server.ServerHttpSecurity

interface AuthorizeExchangeSpecPostProcessor {
    fun authorizeHttpRequests(spec: ServerHttpSecurity.AuthorizeExchangeSpec): ServerHttpSecurity.AuthorizeExchangeSpec
}

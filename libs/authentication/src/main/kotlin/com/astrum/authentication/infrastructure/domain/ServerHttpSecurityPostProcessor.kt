package com.astrum.authentication.infrastructure.domain

import org.springframework.security.config.web.server.ServerHttpSecurity

interface ServerHttpSecurityPostProcessor {
    fun process(serverHttpSecurity: ServerHttpSecurity): ServerHttpSecurity
}

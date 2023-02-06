package com.astrum.authentication.infrastructure.domain

import org.springframework.security.core.Authentication

/**
 * Common interface for test authentication builders
 *
 * @author Yuniel Acosta
 *
 * @param <T> capture for extending class type
</T> */
interface AuthenticationBuilder<T : Authentication> {
    fun build(): T
}

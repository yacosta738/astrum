package com.astrum.authentication

import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.function.Consumer
import java.util.stream.*


open class MockAuthenticationBuilder<A : Authentication, T : MockAuthenticationBuilder<A, T>> {

    private val authMock: A

    constructor(authType: Class<A>, principal: Object) :
            this(authType, principal, principal, principal)

    constructor(authType: Class<A>, principal: Any, details: Any, credentials: Any) {
        this.authMock = mock(authType)
        name(Defaults.AUTH_NAME)
        authorities(Defaults.AUTHORITIES.stream())
        setAuthenticated(true)
        principal(principal)
        details(details)
        credentials(credentials)
    }

    fun build(): A {
        return authMock
    }

    fun authorities(vararg authorities: String): T {
        return authorities(Stream.of(*authorities))
    }

    fun authorities(authorities: Stream<String>): T {
        `when`(authMock.authorities).thenReturn(
            authorities.map { SimpleGrantedAuthority(it) }
                .collect(Collectors.toSet()) as Collection<SimpleGrantedAuthority>
        )
        return downcast()
    }

    fun name(name: String): T {
        `when`(authMock.name).thenReturn(name)
        return downcast()
    }

    fun credentials(credentials: Any): T {
        `when`(authMock.credentials).thenReturn(credentials)
        return downcast()
    }

    fun details(details: Any): T {
        `when`(authMock.details).thenReturn(details)
        return downcast()
    }

    fun principal(principal: Any): T {
        `when`(authMock.principal).thenReturn(principal)
        return downcast()
    }

    fun setAuthenticated(authenticated: Boolean): T {
        `when`(authMock.isAuthenticated).thenReturn(authenticated)
        return downcast()
    }

    fun configure(authConsumer: Consumer<A>): T {
        authConsumer.accept(authMock)
        return downcast()
    }

    @Suppress("UNCHECKED_CAST")
    protected fun downcast(): T {
        return this as T
    }

}

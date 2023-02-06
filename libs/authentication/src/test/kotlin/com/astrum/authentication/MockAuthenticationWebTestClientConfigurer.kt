package com.astrum.authentication

import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.stream.Collectors
import java.util.stream.Stream

class MockAuthenticationWebTestClientConfigurer<T : Authentication>(private val authMock: T) :
    AuthenticationConfigurer<T> {

    override fun build(): T = authMock

    fun authorities(vararg authorities: String): MockAuthenticationWebTestClientConfigurer<T> =
        authorities(Stream.of(*authorities))

    fun authorities(authorities: Stream<String>): MockAuthenticationWebTestClientConfigurer<T> {
        `when`(authMock.authorities).thenReturn(
            authorities.map { SimpleGrantedAuthority(it) }.collect(
                Collectors.toSet()
            )
        )
        return this
    }

    fun name(name: String): MockAuthenticationWebTestClientConfigurer<T> {
        `when`(authMock.name).thenReturn(name)
        return this
    }

    fun credentials(credentials: Any): MockAuthenticationWebTestClientConfigurer<T> {
        `when`(authMock.credentials).thenReturn(credentials)
        return this
    }

    fun details(details: Any): MockAuthenticationWebTestClientConfigurer<T> {
        `when`(authMock.details).thenReturn(details)
        return this
    }

    fun principal(principal: Any): MockAuthenticationWebTestClientConfigurer<T> {
        `when`(authMock.principal).thenReturn(principal)
        return this
    }

    fun setAuthenticated(authenticated: Boolean): MockAuthenticationWebTestClientConfigurer<T> {
        `when`(authMock.isAuthenticated).thenReturn(authenticated)
        return this
    }

    companion object {
        fun <T : Authentication> mockAuthentication(
            authType: Class<T>,
            authMockConfigurer: (T) -> Unit
        ): MockAuthenticationWebTestClientConfigurer<T> {
            val authMock = authMock(authType)
            authMockConfigurer.invoke(authMock)
            return MockAuthenticationWebTestClientConfigurer(authMock)
        }

        fun mockAuthentication(): MockAuthenticationWebTestClientConfigurer<Authentication> =
            mockAuthentication(Authentication::class.java) { }

        fun <T : Authentication> mockAuthentication(authType: Class<T>): MockAuthenticationWebTestClientConfigurer<T> =
            mockAuthentication(authType) { }

        private fun <T : Authentication> authMock(authType: Class<T>): T {
            val auth = mock(authType)
            `when`(auth.authorities).thenReturn(Defaults.GRANTED_AUTHORITIES)
            `when`(auth.name).thenReturn(Defaults.AUTH_NAME)
            `when`(auth.isAuthenticated).thenReturn(true)
            return auth
        }
    }
}

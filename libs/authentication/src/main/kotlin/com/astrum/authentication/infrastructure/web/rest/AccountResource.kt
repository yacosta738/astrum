package com.astrum.authentication.infrastructure.web.rest

import com.astrum.authentication.infrastructure.entities.dto.AdminUserDTO
import com.astrum.authentication.infrastructure.service.UserService
import mu.KotlinLogging
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.security.Principal

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/api")
class AccountResource(private val userService: UserService) {

    internal class AccountResourceException(message: String) : RuntimeException(message)

    private val log = KotlinLogging.logger {}

    /**
     * `GET  /account` : get the current user.
     *
     * @param principal the current user; resolves to `null` if not authenticated.
     * @return the current user.
     * @throws AccountResourceException `500 (Internal Server Error)` if the user couldn't be returned.
     */
    @GetMapping("/account")
    fun getAccount(principal: Principal?): Mono<AdminUserDTO> =
        if (principal is AbstractAuthenticationToken) {
            userService.getUserFromAuthentication(principal)
        } else {
            throw AccountResourceException("User could not be found")
        }

    /**
     * {@code GET  /authenticate} : check if the user is authenticated, and return its login.
     *
     * @param request the HTTP request.
     * @return the login if the user is authenticated.
     */
    @GetMapping("/authenticate")
    fun isAuthenticated(request: ServerWebExchange): Mono<String?> {
        log.debug("REST request to check if the current user is authenticated")
        return request.getPrincipal<Principal>().map(Principal::getName)
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}

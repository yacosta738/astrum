package com.astrum.authentication.infrastructure.web.rest

import com.astrum.authentication.infrastructure.entities.dto.UserDTO
import com.astrum.authentication.infrastructure.service.UserService
import com.astrum.authentication.infrastructure.util.PaginationUtil
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api")
class PublicUserResource(
    private val userService: UserService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * {@code GET /users} : get all users with only the public informations - calling this are allowed for anyone.
     *
     * @param request a {@link ServerHttpRequest} request.
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body all users.
     */
    @GetMapping("/users")
    fun getAllPublicUsers(
        request: ServerHttpRequest,
        @org.springdoc.api.annotations.ParameterObject pageable: Pageable
    ): Mono<ResponseEntity<Flux<UserDTO>>> {
        log.debug("REST request to get all public User names")

        return userService.countManagedUsers()
            .map { PageImpl<UserDTO>(listOf(), pageable, it) }
            .map {
                PaginationUtil.generatePaginationHttpHeaders(
                    UriComponentsBuilder.fromHttpRequest(
                        request
                    ), it
                )
            }
            .map { ResponseEntity.ok().headers(it).body(userService.getAllPublicUsers(pageable)) }
    }

    /**
     * Gets a list of all roles.
     * @return a string list of all roles.
     */
    @GetMapping("/authorities")
    fun getAuthorities() = userService.getAuthorities().collectList()
}

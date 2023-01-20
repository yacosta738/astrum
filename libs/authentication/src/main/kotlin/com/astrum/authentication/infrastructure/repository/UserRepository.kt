package com.astrum.authentication.infrastructure.repository


import com.astrum.authentication.infrastructure.entities.User
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Spring Data Neo4j repository for the {@link User} entity.
 */
@Repository
interface UserRepository : ReactiveNeo4jRepository<User, String> {

    fun findOneByLogin(login: String): Mono<User>

    override fun findAll(): Flux<User>

    fun findAllByIdNotNull(pageable: Pageable): Flux<User>

    fun findAllByIdNotNullAndActivatedIsTrue(pageable: Pageable): Flux<User>

    override fun count(): Mono<Long>
}

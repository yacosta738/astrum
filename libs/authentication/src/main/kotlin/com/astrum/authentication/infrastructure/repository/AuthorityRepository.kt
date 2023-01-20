package com.astrum.authentication.infrastructure.repository

import com.astrum.authentication.infrastructure.entities.Authority
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

/**
 * Spring Data Neo4j repository for the {@link Authority} entity.
 */
@Repository
interface AuthorityRepository : ReactiveNeo4jRepository<Authority, String> {
    override fun findAll(): Flux<Authority>
}

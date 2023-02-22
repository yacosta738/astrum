package com.astrum.data.repository.neo4j

import com.astrum.data.entity.Person
import com.astrum.data.repository.QueryableRepositoryTestHelper
import com.astrum.data.repository.neo4j.migration.CreatePerson
import com.astrum.data.test.Neo4jTestHelper
import com.astrum.ulid.ULID
import com.google.common.cache.CacheBuilder
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate
import java.time.Duration

class InMemoryNeo4jCacheRepositoryTest : QueryableRepositoryTestHelper(
    repositories = {
        listOf(
            Neo4jRepositoryBuilder<Person, ULID>(neo4jTemplate, Person::class)
                .enableCache {
                    CacheBuilder.newBuilder()
                        .softValues()
                        .expireAfterAccess(Duration.ofMinutes(2))
                        .expireAfterWrite(Duration.ofMinutes(5))
                        .maximumSize(1_000)
                }
                .build()
        )
    }
) {
    init {
        migrationManager.register(CreatePerson(neo4jTemplate))
    }

    companion object {
        private val helper = Neo4jTestHelper()

        val neo4jTemplate: ReactiveNeo4jTemplate
            get() = helper.reactiveNeo4jTemplate

        @BeforeAll
        @JvmStatic
        fun setUpAll() = helper.setUp()

        @AfterAll
        @JvmStatic
        fun tearDownAll() = helper.tearDown()
    }
}

package com.astrum.data.repository.neo4j

import com.astrum.data.configuration.Neo4jConfiguration
import com.astrum.data.entity.Person
import com.astrum.data.repository.QueryableRepositoryTestHelper
import com.astrum.data.test.Neo4jTestHelper
import com.astrum.ulid.ULID
import com.google.common.cache.CacheBuilder
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate
import java.time.Duration

@SpringBootTest(classes = [Neo4jConfiguration::class, ReactiveNeo4jTemplate::class])
class InMemoryNeo4jCacheRepositoryTest(
    @Autowired val reactiveNeo4jTemplate: ReactiveNeo4jTemplate
) : QueryableRepositoryTestHelper(
    repositories = {
        listOf(
            Neo4jRepositoryBuilder<Person, ULID>(reactiveNeo4jTemplate, Person::class)
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
    companion object {
        private val helper = Neo4jTestHelper()

        @BeforeAll
        @JvmStatic
        fun setUpAll() = helper.setUp()

        @AfterAll
        @JvmStatic
        fun tearDownAll() = helper.tearDown()
    }
}

package com.astrum.data.repository.neo4j

import com.astrum.data.configuration.Neo4jConfiguration
import com.astrum.data.converter.ULIDToValueConverter
import com.astrum.data.entity.Person
import com.astrum.data.repository.QueryableRepositoryTestHelper
import com.astrum.data.repository.neo4j.migration.CreatePerson
import com.astrum.data.test.Neo4jTestHelper
import com.astrum.ulid.ULID
import com.google.common.cache.CacheBuilder
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Duration

@ExtendWith(SpringExtension::class)
@ComponentScan(basePackages = ["com.astrum.data"])
@ContextConfiguration(classes = [Neo4jConfiguration::class, ULIDToValueConverter::class])
class InMemoryNeo4jCacheRepositoryTest @Autowired constructor(
    private var reactiveNeo4jTemplate: ReactiveNeo4jTemplate
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
    init {
        migrationManager.register(
            CreatePerson(reactiveNeo4jTemplate)
        )
    }

    companion object {
        private val helper = Neo4jTestHelper()

        @BeforeAll
        @JvmStatic
        fun setUpAll() = helper.setUp().also { println("✅➕✅➕✅➕✅") }

        @AfterAll
        @JvmStatic
        fun tearDownAll() = helper.tearDown().also { println("❌➖❌➖❌➖❌") }
    }
}

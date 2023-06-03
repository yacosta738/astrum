package com.astrum.data.repository.neo4j

import com.astrum.data.configuration.Neo4jConfiguration
import com.astrum.data.converter.ULIDToValueConverter
import com.astrum.data.entity.Person
import com.astrum.data.jackson.instant.InstantEpochTimeModule
import com.astrum.data.repository.QueryableRepositoryTestHelper
import com.astrum.data.repository.neo4j.migration.CreatePerson
import com.astrum.data.test.Neo4jTestHelper
import com.astrum.data.test.RedisTestHelper
import com.astrum.ulid.ULID
import com.astrum.ulid.jackson.ULIDModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.common.cache.CacheBuilder
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.extension.ExtendWith
import org.redisson.api.RedissonClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Duration
import java.time.Instant

@ExtendWith(SpringExtension::class)
@ComponentScan(basePackages = ["com.astrum.data"])
@ContextConfiguration(classes = [Neo4jConfiguration::class, ULIDToValueConverter::class])
class RedisCacheRepositoryTest @Autowired constructor(
    private var reactiveNeo4jTemplate: ReactiveNeo4jTemplate
) : QueryableRepositoryTestHelper(
    repositories = {
        listOf(
            Neo4jRepositoryBuilder<Person, ULID>(reactiveNeo4jTemplate, Person::class)
                .enableJsonMapping(
                    jacksonObjectMapper().apply {
                        registerModule(ULIDModule())
                        registerModule(InstantEpochTimeModule())
                    }
                )
                .enableCache(redisClient, expiredAt = { Instant.now().plus(Duration.ofMinutes(30)) }, size = 1000)
                .enableCache {
                    CacheBuilder.newBuilder()
                        .softValues()
                        .expireAfterAccess(Duration.ofMinutes(2))
                        .expireAfterWrite(Duration.ofMinutes(5))
                        .maximumSize(1_000)
                }.build()
        )
    }
) {
    init {
        migrationManager.register(CreatePerson(reactiveNeo4jTemplate))
    }

    companion object {
        private val redisHelper = RedisTestHelper()
        private val mongoHelper = Neo4jTestHelper()

        val redisClient: RedissonClient
            get() = redisHelper.redisClient

        @BeforeAll
        @JvmStatic
        fun setUpAll() {
            redisHelper.setUp()
            mongoHelper.setUp()
        }

        @AfterAll
        @JvmStatic
        fun tearDownAll() {
            redisHelper.tearDown()
            mongoHelper.tearDown()
        }
    }
}

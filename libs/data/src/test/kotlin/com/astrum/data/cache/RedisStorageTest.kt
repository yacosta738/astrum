package com.astrum.data.cache

import com.astrum.data.entity.Person
import com.astrum.data.test.RedisTestHelper
import com.astrum.ulid.ULID
import com.astrum.ulid.jackson.ULIDModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.redisson.api.RedissonClient
import java.time.Duration
import java.time.Instant

class RedisStorageTest : StorageTestHelper(
    run {
        val objectMapper = jacksonObjectMapper().apply {
            registerModule(ULIDModule())
        }

        RedisStorage(
            redisClient,
            name = "test",
            size = 1000,
            objectMapper = objectMapper,
            id = { it.id },
            expiredAt = { Instant.now().plus(Duration.ofMinutes(30)) },
            keyClass = ULID::class,
            valueClass = Person::class,
        )
    }
) {
    companion object {
        private val helper = RedisTestHelper()

        val redisClient: RedissonClient
            get() = helper.redisClient

        @BeforeAll
        @JvmStatic
        fun setUpAll() = helper.setUp()

        @AfterAll
        @JvmStatic
        fun tearDownAll() = helper.tearDown()
    }
}

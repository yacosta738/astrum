package com.astrum.data.repository.r2dbc

import com.astrum.data.entity.Person
import com.astrum.data.repository.TransactionalQueryableRepositoryTestHelper
import com.astrum.data.repository.r2dbc.migration.CreatePerson
import com.astrum.ulid.ULID
import com.google.common.cache.CacheBuilder
import java.time.Duration

class InMemoryR2DBCCacheRepositoryTest : TransactionalQueryableRepositoryTestHelper(
    repositories = {
        listOf(
            R2DBCRepositoryBuilder<Person, ULID>(it.entityOperations, Person::class)
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
        migrationManager.register(CreatePerson(entityOperations))
    }
}

package com.astrum.data.cache

import com.astrum.data.WeekProperty
import com.astrum.data.entity.Person
import com.astrum.ulid.ULID
import com.google.common.cache.CacheBuilder

class PoolingNestedStorageTest : NestedStorageTestHelper(
    run {
        val idProperty = object : WeekProperty<Person, ULID?> {
            override fun get(entity: Person): ULID {
                return entity.id
            }
        }
        PoolingNestedStorage(
            Pool {
                InMemoryStorage(
                    { CacheBuilder.newBuilder() },
                    idProperty
                )
            },
            idProperty
        )
    }
)

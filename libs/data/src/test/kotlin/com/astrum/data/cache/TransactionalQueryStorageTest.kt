package com.astrum.data.cache

import com.astrum.data.entity.Person
import com.google.common.cache.CacheBuilder

class TransactionalQueryStorageTest : QueryStorageTestHelper(
    TransactionalQueryStorage(
        PoolingNestedQueryStorage(
            Pool { InMemoryQueryStorage(Person::class) { CacheBuilder.newBuilder() } }
        )
    )
)

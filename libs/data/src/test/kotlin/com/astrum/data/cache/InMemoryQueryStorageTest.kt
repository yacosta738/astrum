package com.astrum.data.cache

import com.astrum.data.entity.Person
import com.google.common.cache.CacheBuilder

class InMemoryQueryStorageTest :
    QueryStorageTestHelper(InMemoryQueryStorage(Person::class) { CacheBuilder.newBuilder() })

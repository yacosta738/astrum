package com.astrum.data.repository.in_memory

import com.astrum.data.entity.Person
import com.astrum.data.repository.RepositoryTestHelper
import com.astrum.ulid.ULID

class SimpleInMemoryRepositoryTest : RepositoryTestHelper<InMemoryRepository<Person, ULID>>(
    repositories = {
        listOf(
            SimpleInMemoryRepository(Person::class)
        )
    }
)

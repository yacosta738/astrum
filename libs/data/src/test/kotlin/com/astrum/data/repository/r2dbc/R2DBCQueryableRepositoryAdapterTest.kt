package com.astrum.data.repository.r2dbc

import com.astrum.data.entity.Person
import com.astrum.data.repository.TransactionalQueryableRepositoryTestHelper
import com.astrum.data.repository.r2dbc.migration.CreatePerson
import com.astrum.ulid.ULID

class R2DBCQueryableRepositoryAdapterTest : TransactionalQueryableRepositoryTestHelper(
    repositories = {
        listOf(R2DBCRepositoryBuilder<Person, ULID>(it.entityOperations, Person::class).build())
    }
) {
    init {
        migrationManager.register(CreatePerson(entityOperations))
    }
}

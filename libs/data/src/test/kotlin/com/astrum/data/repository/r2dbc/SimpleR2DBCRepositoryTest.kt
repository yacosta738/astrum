package com.astrum.data.repository.r2dbc

import com.astrum.data.entity.Person

class SimpleR2DBCRepositoryTest : R2DBCRepositoryTestHelper(
    repositories = {
        listOf(
            SimpleR2DBCRepository(
                EntityManager(
                    it.entityOperations,
                    Person::class
                )
            )
        )
    }
)

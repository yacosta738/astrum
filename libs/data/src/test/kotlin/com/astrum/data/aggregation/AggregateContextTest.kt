package com.astrum.data.aggregation

import com.astrum.data.criteria.where
import com.astrum.data.dummy.DummyPerson
import com.astrum.data.entity.Person
import com.astrum.data.repository.r2dbc.R2DBCRepositoryBuilder
import com.astrum.data.repository.r2dbc.migration.CreatePerson
import com.astrum.data.test.DataTestHelper
import com.astrum.ulid.ULID
import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AggregateContextTest : DataTestHelper() {
    private val repository =
        spyk(R2DBCRepositoryBuilder<Person, ULID>(entityOperations, Person::class).build())
    private val context = AggregateContext(repository, Person::class)

    init {
        migrationManager.register(CreatePerson(entityOperations))
    }

    @BeforeEach
    override fun setUp() {
        super.setUp()

        blocking {
            context.clear()
        }
    }

    @Test
    fun join() = blocking {
        val person1 = DummyPerson.create()
            .let { repository.create(it) }
        val person2 = DummyPerson.create()
            .let { repository.create(it) }

        val query1 = where(Person::name).`is`(person1.name)
        val query2 = where(Person::name).`is`(person2.name)

        val fetcher1 = context.join(query1)
        val fetcher2 = context.join(query2)

        val result1 = fetcher1.fetch().toList()
        val result2 = fetcher2.fetch().toList()

        assertEquals(1, result1.size)
        assertEquals(person1, result1[0])
        assertEquals(1, result2.size)
        assertEquals(person2, result2[0])

        coVerify(exactly = 1) { repository.findAll(any()) }

        fetcher1.fetch().toList()
        coVerify(exactly = 2) { repository.findAll(any()) }
        fetcher2.fetch().toList()
        coVerify(exactly = 2) { repository.findAll(any()) }

        fetcher1.fetch().toList()
        fetcher1.fetch().toList()
        coVerify(exactly = 4) { repository.findAll(any()) }
    }
}

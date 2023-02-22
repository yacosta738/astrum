package com.astrum.data.cache

import com.astrum.coroutine.test.CoroutineTestHelper
import com.astrum.data.criteria.where
import com.astrum.data.dummy.DummyPerson
import com.astrum.data.entity.Person
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class QueryStorageTestHelper(private val storage: QueryStorage<Person>) :
    CoroutineTestHelper() {
    @BeforeEach
    override fun setUp() {
        super.setUp()

        blocking {
            storage.clear()
        }
    }

    @Test
    fun getIfPresent() = blocking {
        val value = DummyPerson.create()
        val query = where(Person::name).`is`(value.name)

        assertNull(storage.getIfPresent(query))
        assertEquals(value, storage.getIfPresent(query) { value })
        assertNull(storage.getIfPresent(SelectQuery(query)))
        assertEquals(listOf(value), storage.getIfPresent(SelectQuery(query)) { listOf(value) })
    }

    @Test
    fun remove() = blocking {
        val value = DummyPerson.create()
        val query = where(Person::name).`is`(value.name)

        storage.put(SelectQuery(query), listOf(value))

        storage.remove(SelectQuery(query))

        assertNull(storage.getIfPresent(SelectQuery(query)))
    }

    @Test
    fun put() = blocking {
        val value = DummyPerson.create()
        val query = where(Person::name).`is`(value.name)

        storage.put(SelectQuery(query), listOf(value))

        assertEquals(listOf(value), storage.getIfPresent(SelectQuery(query)))
    }

    @Test
    fun clear() = blocking {
        val value = DummyPerson.create()
        val query = where(Person::name).`is`(value.name)

        storage.put(SelectQuery(query), listOf(value))
        storage.clear()

        assertNull(storage.getIfPresent(SelectQuery(query)))

        storage.put(SelectQuery(query), listOf(value))
        storage.clear(value)

        assertNull(storage.getIfPresent(SelectQuery(query)))

        storage.put(SelectQuery(query), listOf(value))
        storage.clear(DummyPerson.create())

        assertNotNull(storage.getIfPresent(SelectQuery(query)))
    }

    @Test
    fun entries() = blocking {
        val value = DummyPerson.create()
        val query = where(Person::name).`is`(value.name)

        storage.put(SelectQuery(query), listOf(value))

        val multi = storage.entries()

        assertEquals(setOf(SelectQuery(query) to listOf(value)), multi)
    }
}

package com.astrum.data.repository

import com.astrum.data.criteria.where
import com.astrum.data.dummy.DummyPerson
import com.astrum.data.entity.Person
import com.astrum.data.patch.Patch
import com.astrum.ulid.ULID
import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

abstract class QueryableRepositoryTestHelper(
    repositories: (RepositoryTestHelper<QueryableRepository<Person, ULID>>) -> List<QueryableRepository<Person, ULID>>,
) : RepositoryTestHelper<QueryableRepository<Person, ULID>>(repositories) {

    @Test
    fun existsByName() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }

        assertTrue(personRepository.exists(where(Person::name).`is`(person.name)))
    }

    @Test
    fun findAllCustomQuery() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val foundPersons = personRepository.findAll(where(Person::id).`is`(person.id)).toList()

        assertEquals(foundPersons.size, 1)
        assertEquals(person.id, foundPersons[0].id)
        assertNotNull(foundPersons[0].createdAt)
        assertNotNull(foundPersons[0].updatedAt)

        assertEquals(person.name, foundPersons[0].name)
        assertEquals(person.age, foundPersons[0].age)
    }

    @Test
    fun findAllByNameIs() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val foundPersons = personRepository.findAll(where(Person::name).`is`(person.name)).toList()

        assertEquals(foundPersons.size, 1)
        assertEquals(person.id, foundPersons[0].id)
        assertNotNull(foundPersons[0].createdAt)
        assertNotNull(foundPersons[0].updatedAt)

        assertEquals(person.name, foundPersons[0].name)
        assertEquals(person.age, foundPersons[0].age)
    }

    @Test
    fun findAllByNameIn() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val foundPersons = personRepository.findAll(where(Person::name).`in`(person.name)).toList()

        assertEquals(1, foundPersons.size)
        assertEquals(person.id, foundPersons[0].id)
        assertNotNull(foundPersons[0].createdAt)
        assertNotNull(foundPersons[0].updatedAt)

        assertEquals(person.name, foundPersons[0].name)
        assertEquals(person.age, foundPersons[0].age)
    }

    @Test
    fun findOneByName() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val foundPerson = personRepository.findOneOrFail(where(Person::name).`is`(person.name))

        assertEquals(person.id, foundPerson.id)
        assertNotNull(foundPerson.createdAt)
        assertNotNull(foundPerson.updatedAt)

        assertEquals(person.name, foundPerson.name)
        assertEquals(person.age, foundPerson.age)
    }

    @Test
    fun updateByName() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val patch = DummyPerson.create()

        val updatedPerson = personRepository.updateOrFail(
            where(Person::name).`is`(person.name)
        ) {
            it.name = patch.name
            it.age = patch.age
        }

        assertEquals(person.id, updatedPerson.id)
        assertEquals(patch.name, updatedPerson.name)
        assertEquals(patch.age, updatedPerson.age)
        assertNotNull(updatedPerson.updatedAt)
    }

    @Test
    fun updateAllByName() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val patch = DummyPerson.create()

        // Dummy test to check if the updateAll method works
//        val allDummyPersonsList = personRepository.findAll(where(Person::name).`is`(person.name)).toList()
//        assertEquals(1, allDummyPersonsList.size)

        val updatedPersons = personRepository.updateAll(
            where(Person::name).`is`(person.name),
            Patch.with {
                it.name = patch.name
                it.age = patch.age
            }
        ).toList()
        // Dummy test to check if the updateAll method works
//        val dummyPersonList = personRepository.findAll(where(Person::name).`is`(person.name)).toList()
//        assertEquals(1, dummyPersonList.size)

        assertEquals(1, updatedPersons.size)
        val updatedPerson = updatedPersons[0]
        assertEquals(person.id, updatedPerson.id)
        assertEquals(patch.name, updatedPerson.name)
        assertEquals(patch.age, updatedPerson.age)
        assertNotNull(updatedPerson.updatedAt)
    }

    @Test
    fun countByName() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }

        assertEquals(1, personRepository.count(where(Person::name).`is`(person.name)))
    }

    @Test
    fun deleteAllByName() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }

        personRepository.deleteAll(where(Person::name).`is`(person.name))
        assertFalse(personRepository.existsById(person.id))
    }
}

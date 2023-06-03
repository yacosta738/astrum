package com.astrum.data.repository.neo4j

import com.astrum.data.criteria.where
import com.astrum.data.dummy.DummyPerson
import com.astrum.data.entity.Person
import com.astrum.data.repository.RepositoryTestHelper
import com.astrum.data.repository.neo4j.migration.CreatePerson
import com.astrum.data.test.Neo4jTestHelper
import com.astrum.ulid.ULID
import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate

abstract class Neo4jRepositoryTestHelper(
    private val reactiveNeo4jTemplate: ReactiveNeo4jTemplate,
    repositories: (RepositoryTestHelper<Neo4jRepository<Person, ULID>>) -> List<Neo4jRepository<Person, ULID>>,
): RepositoryTestHelper<Neo4jRepository<Person, ULID>>(repositories) {
    private val parser = Neo4jCriteriaParser(Person::class)
    init {
        migrationManager.register(CreatePerson(reactiveNeo4jTemplate))
    }

    @Test
    fun existsByName() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }

        assertTrue(personRepository.exists(parser.parse(where(Person::name).`is`(person.name))))
    }

    @Test
    fun findAllCustomQuery() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val foundPersons = personRepository.findAll(parser.parse(where(Person::id).`is`(person.id))).toList()

        assertEquals(foundPersons.size, 1)
        assertEquals(person.id, foundPersons[0].id)

        assertEquals(person.name, foundPersons[0].name)
        assertEquals(person.age, foundPersons[0].age)
    }

    @Test
    fun findAllByNameIs() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val foundPersons = personRepository.findAll(parser.parse(where(Person::name).`is`(person.name))).toList()

        assertEquals(foundPersons.size, 1)
        assertEquals(person.id, foundPersons[0].id)

        assertEquals(person.name, foundPersons[0].name)
        assertEquals(person.age, foundPersons[0].age)
    }

    @Test
    fun findAllByNameIn() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val foundPersons = personRepository.findAll(parser.parse(where(Person::name).`in`(person.name))).toList()

        assertEquals(foundPersons.size, 1)
        assertEquals(person.id, foundPersons[0].id)

        assertEquals(person.name, foundPersons[0].name)
        assertEquals(person.age, foundPersons[0].age)
    }

    @Test
    fun findOneByName() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val foundPerson = personRepository.findOneOrFail(parser.parse(where(Person::name).`is`(person.name)))

        assertEquals(person.id, foundPerson.id)

        assertEquals(person.name, foundPerson.name)
        assertEquals(person.age, foundPerson.age)
    }

    @Test
    fun updateByName() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val patch = DummyPerson.create()

        val updatedPerson = personRepository.updateOrFail(parser.parse(where(Person::name).`is`(person.name))) {
            it.name = patch.name
            it.age = patch.age
        }

        assertEquals(person.id, updatedPerson.id)
        assertEquals(patch.name, updatedPerson.name)
        assertEquals(patch.age, updatedPerson.age)
        assertNotNull(updatedPerson.updatedAt)
    }


    @Test
    fun countByName() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }

        assertEquals(1, personRepository.count(parser.parse(where(Person::name).`is`(person.name))))
    }

    @Test
    fun deleteAllByName() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }

        personRepository.deleteAll(parser.parse(where(Person::name).`is`(person.name)))
        assertFalse(personRepository.existsById(person.id))
    }

    companion object {
        private val helper = Neo4jTestHelper()

        @BeforeAll
        @JvmStatic
        fun setUpAll() = helper.setUp()

        @AfterAll
        @JvmStatic
        fun tearDownAll() = helper.tearDown()
    }
}
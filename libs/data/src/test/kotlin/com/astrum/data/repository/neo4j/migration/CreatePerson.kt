package com.astrum.data.repository.neo4j.migration

import com.astrum.data.entity.Person
import com.astrum.data.migration.Migration
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate

class CreatePerson(
    private val neo4jTemplate: ReactiveNeo4jTemplate
) : Migration {
    override suspend fun up() {
        val person = Person("test", 10)
        println("Create Person $person with id ${person.id}")
        neo4jTemplate.save<Person>(person).awaitFirstOrNull()
    }

    override suspend fun down() {
        neo4jTemplate.deleteAll(Person::class.java).awaitFirstOrNull()
    }
}

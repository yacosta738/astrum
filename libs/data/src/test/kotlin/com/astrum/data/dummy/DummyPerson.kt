package com.astrum.data.dummy

import com.astrum.data.entity.Person
import com.astrum.util.resolveNotNull
import com.astrum.util.username
import net.datafaker.Faker
import java.security.SecureRandom
import java.util.*

object DummyPerson {
    data class PersonTemplate(
        val name: Optional<String>? = null,
        val age: Optional<Int>? = null,
    )

    private val faker = Faker(SecureRandom())

    fun create(template: PersonTemplate? = null): Person {
        return Person(
            name = resolveNotNull(template?.name) { faker.name().username(15) },
            age = resolveNotNull(template?.age) { faker.number().numberBetween(0, 100) },
        )
    }
}

package com.astrum.talentum.unit.user

import com.astrum.talentum.user.Name
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.stream.Stream

class NameTest {

    @Test
    fun `should create a valid name and lastname`() {
        val names = mapOf<String, String>(
            "Yuniel" to "Acosta Pérez",
            "Neil" to "O'Neil",
            "Ramón" to "González Pérez",
            "Ñico" to "López",
        )

        names.forEach { (firstname, lastname) ->
            println("Fullname: $firstname $lastname")
            val name = Name(firstname, lastname)
            assertEquals(firstname, name.firstName.value)
            assertEquals(lastname, name.lastName.value)
            assertEquals(name.fullName(), "$firstname $lastname")
        }
    }

    @Test
    fun `should not build without firstname`() {
        assertThrows(IllegalArgumentException::class.java) {
            Name("", "Acosta Pérez")
        }
    }

    @Test
    fun `should not build without lastname`() {
        assertThrows(IllegalArgumentException::class.java) {
            Name("Yuniel", "")
        }
    }

    @Test
    fun `should not build with firstname greater than 150 characters`() {
        val firstname = (1..256).joinToString("") { "a" }
        assertThrows(IllegalArgumentException::class.java) {
            Name(firstname, "Acosta Pérez")
        }
    }

    @Test
    fun `should not build with lastname greater than 150 characters`() {
        val lastname = (1..256).joinToString("") { "a" }
        assertThrows(IllegalArgumentException::class.java) {
            Name("Yuniel", lastname)
        }
    }

    @Test
    fun `should not build with firstname and lastname greater than 150 characters`() {
        val firstname = (1..256).joinToString("") { "a" }
        val lastname = (1..256).joinToString("") { "a" }
        assertThrows(IllegalArgumentException::class.java) {
            Name(firstname, lastname)
        }
    }

    @Test
    fun `should get fullname`() {
        val name = Name("Yuniel", "Acosta Pérez")
        assertEquals("Yuniel Acosta Pérez", name.fullName())
    }

    @Test
    fun shouldSortNames() {
        val names: List<Name> = Stream
            .of(Name("paul", "Dupond"), Name("jean", "Dupont"), Name("jean", "Dupond"))
            .sorted()
            .toList()
        assertThat(names).containsExactly(
            Name("jean", "Dupond"),
            Name("jean", "Dupont"),
            Name("paul", "Dupond")
        )
    }
}
package com.astrum.talentum.unit.user

import com.astrum.talentum.user.LastName
import com.astrum.talentum.user.exceptions.LastNameNotValidException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LastNameTest {
    @Test
    fun `should create a valid last name`() {
        val lastNames = listOf(
            "Acosta", "Acosta Ortiz", "Acosta Pérez", "O'Neil",
            "D'Angelo"
        )
        lastNames.forEach {
            println("Last Name: $it")
            val lastName = LastName(it)
            assertEquals(it, lastName.value)
        }
    }

    @Test
    fun `should throw an exception when create a last name with invalid value`() {
        val invalidLastNames = listOf(
            "A voluptatum ex. Ratione adipisci eveniet expedita iste consectetur voluptatibus illum odio optio. Eum facilis autem. Explicabo sequi corrupti eius quis eius adipisci."
        )
        invalidLastNames.forEach {
            println("Last Name: $it")
            assertThrows(LastNameNotValidException::class.java) {
                LastName(it)
            }
        }
    }

    @Test
    fun `should throw an exception when create a last name with empty value`() {
        assertThrows(LastNameNotValidException::class.java) {
            LastName("")
        }
    }

    @Test
    fun `should throw an exception when create a last name with blank value`() {
        assertThrows(LastNameNotValidException::class.java) {
            LastName(" ")
        }
    }

    @Test
    fun `should throw an exception when create a last name with length greater than 150`() {
        val lastName = (1..256).joinToString("") { "a" }
        assertThrows(LastNameNotValidException::class.java) {
            LastName(lastName)
        }
    }

    @Test
    fun `compare last name`() {
        val lastName1 = LastName("Acosta")
        val lastName2 = LastName("Acosta")
        assertEquals(lastName1, lastName2)
    }

    @Test
    fun `compare last name with different values`() {
        val lastName1 = LastName("Acosta")
        val lastName2 = LastName("Acosta Ortiz")
        assertNotEquals(lastName1, lastName2)
        assertNotEquals(0, lastName1.compareTo(lastName2))
        assertNotEquals(lastName1.hashCode(), lastName2.hashCode())
    }
}
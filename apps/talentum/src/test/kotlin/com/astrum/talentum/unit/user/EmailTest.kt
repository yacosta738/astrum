package com.astrum.talentum.unit.user

import com.astrum.talentum.user.Email
import com.astrum.talentum.user.exceptions.EmailNotValidException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class EmailTest {
    @Test
    fun `should create email`() {
        val email = Email("john.snow@gmail.com")
        assertEquals("john.snow@gmail.com", email.email)
    }

    @Test
    fun `should throw exception when email is not valid`() {
        val invalidEmails = listOf(
            "john.snow@gmail.", "Julia.abc@",
            "Julia.abc@.com", "Samantha_21.", ".1Samantha",
            "Samantha@10_2A", "JuliaZ007", "_Julia007.com",
            "Willie_Zboncak@@yahoo.com"
        )

        invalidEmails.forEach {
            println("Email: $it")
            assertThrows(EmailNotValidException::class.java) {
                Email(it)
            }
        }
    }

    @Test
    fun `should throw exception when email is empty`() {
        assertThrows(EmailNotValidException::class.java) {
            Email("")
        }
    }

    @Test
    fun `should throw exception when email is blank`() {
        assertThrows(EmailNotValidException::class.java) {
            Email(" ")
        }
    }

    @Test
    fun `should throw exception when email length is greater than 255`() {
        val email = "john.snow@${
            (1..256).joinToString("") { "a" }
        }.com"
        assertThrows(EmailNotValidException::class.java) {
            Email(email)
        }
    }

    @Test
    fun `compare email`() {
        val email1 = Email("john.snow@gmail.com")
        val email2 = Email("john.snow@gmail.com")
        assertEquals(email1, email2)
        assertEquals(0, email1.compareTo(email2))
        assertEquals(email1.hashCode(), email2.hashCode())
    }

    @Test
    fun `compare email with different value`() {
        val email1 = Email("john.snow@gmail.com")
        val email2 = Email("john-snow@gmail.com")
        assertNotEquals(email1, email2)
        assertNotEquals(0, email1.compareTo(email2))
        assertNotEquals(email1.hashCode(), email2.hashCode())
    }
}
package com.astrum.talentum.unit.authentication.domain

import com.astrum.authentication.domain.Username
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class UsernameTest {
    @Test
    fun `should get empty username from empty username`() {
        assertThat(Username.of("")).isEmpty
    }

    @Test
    fun `should get empty username from blank username`() {
        assertThat(Username.of(" ")).isEmpty
    }

    @Test
    fun `should get username from actual username`() {
        assertThat(Username.of("user")).contains(Username("user"))
    }

    @Test
    fun `should get username`() {
        assertThat(Username("user").username).isEqualTo("user")
    }
}

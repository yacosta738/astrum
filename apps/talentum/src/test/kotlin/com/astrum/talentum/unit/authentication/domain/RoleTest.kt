package com.astrum.talentum.unit.authentication.domain

import com.astrum.authentication.domain.Role
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class RoleTest {
    @Test
    fun `should get role key`() {
        assertThat(Role.ADMIN.key()).isEqualTo("ROLE_ADMIN")
    }

    @Test
    fun `should convert unknown role to unknown role`() {
        assertThat(Role.from("ROLE_DUMMY")).isEqualTo(Role.UNKNOWN)
    }

    @Test
    fun `should convert from role`() {
        assertThat(Role.from("ROLE_ADMIN")).isEqualTo(Role.ADMIN)
    }
}

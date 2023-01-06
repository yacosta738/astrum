package com.astrum.talentum.unit.authentication.domain

import com.astrum.authentication.domain.Role
import com.astrum.authentication.domain.Roles
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test


class RolesTest {

    @Test
    fun `hasRole should return true when role is present`() {
        val roles = Roles(setOf(Role.ADMIN))
        assertTrue(roles.hasRole(Role.ADMIN))
    }

    @Test
    fun `hasRole should return false when role is not present`() {
        val roles = Roles(setOf(Role.ADMIN))
        assertFalse(roles.hasRole(Role.USER))
    }

    @Test
    fun `hasRole should return false when roles is empty`() {
        val roles = Roles(setOf<Role>())
        assertFalse(roles.hasRole(Role.USER))
    }

    @Test
    fun `hasRole any rol`() {
        val element = Role.values().toList()
        val roles = Roles(element.toSet())
        assertTrue(roles.hasRole(Role.USER))
        assertTrue(roles.hasRole(Role.ADMIN))
        assertTrue(roles.hasRole(Role.ANONYMOUS))
        assertTrue(roles.hasRole(Role.UNKNOWN))
        assertTrue(roles.hasRole())
    }

    @Test
    fun `hasRole any rol with empty roles`() {
        val roles = Roles(setOf<Role>())
        assertFalse(roles.hasRole(Role.USER))
        assertFalse(roles.hasRole(Role.ADMIN))
        assertFalse(roles.hasRole(Role.ANONYMOUS))
        assertFalse(roles.hasRole(Role.UNKNOWN))
        assertFalse(roles.hasRole())
    }

    @Test
    fun `hasRole any rol build with key roles`() {
        val systemRoles = listOf(
            Role.from("ROLE_ADMIN"),
            Role.from("ROLE_USER"),
            Role.from("ROLE_ANONYMOUS"),
            Role.from("ROLE_UNKNOWN")
        )
        val roles = Roles(systemRoles.toSet())
        assertTrue(roles.hasRole(Role.USER))
        assertTrue(roles.hasRole(Role.ADMIN))
        assertTrue(roles.hasRole(Role.ANONYMOUS))
        assertTrue(roles.hasRole(Role.UNKNOWN))
        assertTrue(roles.hasRole())
    }

    @Test
    fun `hasRole any rol build with key roles with empty roles`() {
        val systemRoles = listOf(
            Role.from(" "),
            Role.from("")
        )
        val roles = Roles(systemRoles.toSet())
        assertFalse(roles.hasRole(Role.USER))
        assertFalse(roles.hasRole(Role.ADMIN))
        assertFalse(roles.hasRole(Role.ANONYMOUS))
        assertTrue(roles.hasRole(Role.UNKNOWN))
        assertTrue(roles.hasRole())
    }

    @Test
    fun `should not have role without roles`() {
        assertThat(Roles(setOf<Role>()).hasRole()).isFalse
    }

    @Test
    fun `should have role with roles`() {
        assertThat(Roles(setOf(Role.ADMIN)).hasRole()).isTrue
    }

    @Test
    fun `should not have not affected role`() {
        assertThat(Roles(setOf(Role.ADMIN)).hasRole(Role.USER)).isFalse
    }

    @Test
    fun `should have affected role`() {
        assertThat(Roles(setOf(Role.ADMIN)).hasRole(Role.ADMIN)).isTrue
    }

    @Test
    fun `should stream roles`() {
        assertThat(Roles(setOf(Role.ADMIN)).stream()).containsExactly(Role.ADMIN)
    }

    @Test
    fun `should get roles`() {
        assertThat(Roles(setOf(Role.ADMIN)).roles).containsExactly(Role.ADMIN)
    }
}
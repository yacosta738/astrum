package com.astrum.authentication.domain

import java.util.stream.Stream

data class Roles(val roles: Set<Role>) {

    constructor(roles: Collection<String>) : this(roles.map { Role.from(it) }.toSet())

    fun hasRole(role: Role): Boolean {
        return roles.contains(role)
    }

    fun hasRole(): Boolean {
        return roles.isNotEmpty()
    }

    fun stream(): Stream<Role> {
        return roles.stream()
    }

    companion object {
        val EMPTY: Roles = Roles(emptySet<Role>())
    }
}
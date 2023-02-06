package com.astrum.authentication.infrastructure.domain


import com.astrum.authentication.infrastructure.exceptions.UnparsableClaimException
import java.io.Serializable
import java.net.URI
import java.net.URISyntaxException
import java.time.Instant

private const val CLAIM_NAME_CANNOT_BE_EMPTY = "claimName can't be empty"

interface ClaimSet : MutableMap<String, Any>, Serializable {
    fun getAsString(name: String): String {
        return this[name]?.toString() ?: ""
    }

    fun getAsInstant(name: String): Instant {
        val claim = this[name] ?: return Instant.EPOCH
        return when (claim) {
            is Long -> Instant.ofEpochSecond(claim)
            is Instant -> claim
            is String -> Instant.parse(claim)
            else -> throw UnparsableClaimException("claim $name is of unsupported type ${claim.javaClass.name}")
        }
    }

    fun getAsStringSet(name: String): Set<String> {
        val claim = this[name] ?: return emptySet()
        return when (claim) {
            is Collection<*> -> claim.flatMap { it.toString().split(" ").asIterable() }.toSet()
            else -> claim.toString().split(" ").toSet()
        }
    }

    @Throws(URISyntaxException::class)
    fun getAsUri(name: String): URI? {
        val claim = this[name] ?: return null
        return when (claim) {
            is URI -> claim
            else -> URI(claim.toString())
        }
    }

    fun getAsBoolean(name: String): Boolean {
        val claim = this[name] ?: return false
        return when (claim) {
            is Boolean -> claim
            else -> claim.toString().toBoolean()
        }
    }

    fun claim(claimName: String, claimValue: String): ClaimSet {
        require(claimName.isNotEmpty()) { CLAIM_NAME_CANNOT_BE_EMPTY }
        if (claimValue.isNotEmpty()) {
            this[claimName] = claimValue
        } else {
            remove(claimName)
        }
        return this
    }

    fun claim(claimName: String, claimValue: Collection<*>): ClaimSet {
        require(claimName.isNotEmpty()) { CLAIM_NAME_CANNOT_BE_EMPTY }
        if (claimValue.isEmpty()) {
            remove(claimName)
        } else {
            this[claimName] = claimValue
        }
        return this
    }

    fun claim(claimName: String, claimValue: Any): ClaimSet {
        require(claimName.isNotEmpty()) { CLAIM_NAME_CANNOT_BE_EMPTY }
        this[claimName] = claimValue
        return this
    }
}

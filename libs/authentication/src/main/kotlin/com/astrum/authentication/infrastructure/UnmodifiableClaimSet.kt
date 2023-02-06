package com.astrum.authentication.infrastructure

import com.astrum.authentication.infrastructure.domain.ClaimSet
import java.util.stream.Collectors

open class UnmodifiableClaimSet(delegate: Map<String, Any>) : Map<String, Any>,
    HashMap<String, Any>(delegate),
    ClaimSet {
    override fun toString(): String {
        return entries.stream().map { (key, value): Map.Entry<String, Any> ->
            String.format(
                "%s => %s",
                key,
                value
            )
        }.collect(Collectors.joining(", ", "[", "]"))
    }

    companion object {
        private const val serialVersionUID = -51499250697429528L
    }
}

package com.astrum.authentication.annotations

import com.astrum.authentication.infrastructure.ModifiableClaimSet


@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Claims(
    val intClaims: Array<IntClaim> = [],
    val longClaims: Array<LongClaim> = [],
    val stringClaims: Array<StringClaim> = [],
    val stringArrayClaims: Array<StringArrayClaim> = [],
    val jsonObjectClaims: Array<JsonObjectClaim> = [],
    val jsonArrayClaims: Array<JsonArrayClaim> = []
) {
    companion object {
        fun of(annotation: Claims): ModifiableClaimSet {
            val claims = ModifiableClaimSet()
            annotation.intClaims.forEach { claims.claim(it.name, it.value) }
            annotation.longClaims.forEach { claims.claim(it.name, it.value) }
            annotation.stringClaims.forEach { claims.claim(it.name, it.value) }
            annotation.stringArrayClaims.forEach { claims.claim(it.name, it.value) }
            annotation.jsonObjectClaims.forEach { claims.claim(it.name, JsonObjectClaim.parse(it)) }
            annotation.jsonArrayClaims.forEach { claims.claim(it.name, JsonArrayClaim.parse(it)) }
            return claims
        }
    }
}

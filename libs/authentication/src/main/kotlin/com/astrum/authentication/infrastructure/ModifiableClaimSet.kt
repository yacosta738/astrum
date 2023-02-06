package com.astrum.authentication.infrastructure

import com.astrum.authentication.infrastructure.domain.ClaimSet

open class ModifiableClaimSet : MutableMap<String, Any>, HashMap<String, Any>, ClaimSet {
    /**
     * @param properties initial values (copied so that "properties" is not altered
     * when claim-set is modified)
     */
    constructor(properties: MutableMap<String, Any>) : super(properties)
    constructor()
    constructor(initialCapacity: Int, loadFactor: Float) : super(initialCapacity, loadFactor)
    constructor(initialCapacity: Int) : super(initialCapacity)

    companion object {
        private const val serialVersionUID = -1967790894352277253L
    }
}

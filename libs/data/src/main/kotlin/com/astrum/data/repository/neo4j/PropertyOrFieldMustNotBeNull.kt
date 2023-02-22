package com.astrum.data.repository.neo4j

class PropertyOrFieldMustNotBeNull : RuntimeException("Property or field must not be null.") {
    companion object {
        private const val serialVersionUID = 174345L
    }
}

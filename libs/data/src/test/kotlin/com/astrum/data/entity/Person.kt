package com.astrum.data.entity

import com.astrum.data.ModifiableULIDEntity
import com.astrum.data.annotation.Key
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.relational.core.mapping.Table

@Table("persons")
@Node("persons")
data class Person(
    @Key
    var name: String,
    var age: Int,
) : ModifiableULIDEntity()

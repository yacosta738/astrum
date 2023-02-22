package com.astrum.data

import com.astrum.data.annotation.GeneratedValue
import com.astrum.ulid.ULID
import org.springframework.data.annotation.Id
import org.springframework.data.neo4j.core.schema.Id  as Neo4jId

abstract class ULIDEntity : Entity<ULID>() {
    @Id
    @Neo4jId
    @GeneratedValue
    override var id: ULID = ULID.randomULID()
}

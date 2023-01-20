package com.astrum.authentication.infrastructure.entities

import jakarta.validation.constraints.*
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import java.io.Serializable

/**
 * An authority (a security role) used by Spring Security.
 */
@Node("authority")
data class Authority(

    @field:NotNull
    @field:Size(max = 50)
    @Id
    var name: String? = null

) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Authority) return false
        if (other.name == null || name == null) return false

        return name == other.name
    }

    override fun hashCode() = 31

    companion object {
        private const val serialVersionUID = 1L
    }
}

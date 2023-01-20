package com.astrum.authentication.infrastructure.entities

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.neo4j.core.schema.Property
import java.io.Serializable
import java.time.Instant

/**
 * Base abstract class for entities which will hold definitions for created, last modified by, created by,
 * last modified by attributes.
 */
@JsonIgnoreProperties(
    value = ["createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate"],
    allowGetters = true
)
abstract class AbstractAuditingEntity<T>(
    @Property("created_by")
    open var createdBy: String? = null,

    @CreatedDate
    @Property("created_date")
    open var createdDate: Instant? = Instant.now(),

    @Property("last_modified_by")
    open var lastModifiedBy: String? = null,

    @LastModifiedDate
    @Property("last_modified_date")
    open var lastModifiedDate: Instant? = Instant.now()
) : Serializable {

    abstract val id: T?

    companion object {
        private const val serialVersionUID = 1L
    }
}

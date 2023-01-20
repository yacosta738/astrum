package com.astrum.authentication.infrastructure.entities

import com.astrum.authentication.infrastructure.config.LOGIN_REGEX
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.constraints.*
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Property
import org.springframework.data.neo4j.core.schema.Relationship
import java.io.Serializable
import java.time.Instant

/**
 * A user.
 */
@Node("user")
class User(
    @Id
    @Property("user_id")
    override var id: String? = null,

    @field:NotNull
    @field:Pattern(regexp = LOGIN_REGEX)
    @field:Size(min = 1, max = 50)
    var login: String? = null,

    @field:Size(max = 50)
    @Property("first_name")
    var firstName: String? = null,

    @field:Size(max = 50)
    @Property("last_name")
    var lastName: String? = null,

    @field:Email
    @field:Size(min = 5, max = 254)
    var email: String? = null,

    var activated: Boolean? = false,

    @field:Size(min = 2, max = 10)
    @Property("lang_key")
    var langKey: String? = null,

    @field:Size(max = 256)
    @Property("image_url")
    var imageUrl: String? = null,
    @JsonIgnore
    @Relationship("HAS_AUTHORITY")

    var authorities: MutableSet<Authority> = mutableSetOf(),
    createdBy: String? = null,
    createdDate: Instant? = Instant.now(),
    lastModifiedBy: String? = null,
    lastModifiedDate: Instant? = Instant.now()
) : AbstractAuditingEntity<String>(createdBy, createdDate, lastModifiedBy, lastModifiedDate),
    Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        if (other.id == null || id == null) return false

        return id == other.id
    }

    override fun hashCode() = 31

    override fun toString() =
        "User{" +
                "login='" + login + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", activated='" + activated + '\'' +
                ", langKey='" + langKey + '\'' +
                "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}

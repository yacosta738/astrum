package com.astrum.authentication.infrastructure.entities.dto

import com.astrum.authentication.infrastructure.config.LOGIN_REGEX
import com.astrum.authentication.infrastructure.entities.User
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.io.Serializable
import java.time.Instant

/**
 * A DTO representing a user, with his authorities.
 */
open class AdminUserDTO(
    var id: String? = null,

    @field:NotBlank
    @field:Pattern(regexp = LOGIN_REGEX)
    @field:Size(min = 1, max = 50)
    var login: String? = null,

    @field:Size(max = 50)
    var firstName: String? = null,

    @field:Size(max = 50)
    var lastName: String? = null,

    @field:Email
    @field:Size(min = 5, max = 254)
    var email: String? = null,

    @field:Size(max = 256)
    var imageUrl: String? = null,

    var activated: Boolean? = false,

    @field:Size(min = 2, max = 10)
    var langKey: String? = null,

    var createdBy: String? = null,

    var createdDate: Instant? = null,

    var lastModifiedBy: String? = null,

    var lastModifiedDate: Instant? = null,

    var authorities: MutableSet<String> = mutableSetOf()
) : Serializable {

    constructor(user: User?) : this(
        user?.id,
        user?.login,
        user?.firstName,
        user?.lastName,
        user?.email,
        user?.imageUrl,
        user?.activated,
        user?.langKey,
        user?.createdBy,
        user?.createdDate,
        user?.lastModifiedBy,
        user?.lastModifiedDate,

        user?.authorities?.map { it.name }?.filterNotNullTo(mutableSetOf()) ?: mutableSetOf()
    )

    override fun toString(): String {
        return "AdminUserDTO{" +
                "login='" + login + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", activated=" + activated +
                ", langKey='" + langKey + '\'' +
                ", authorities=" + authorities +
                "}"
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}

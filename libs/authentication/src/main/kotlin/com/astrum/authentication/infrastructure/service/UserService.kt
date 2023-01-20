package com.astrum.authentication.infrastructure.service

import com.astrum.authentication.infrastructure.config.DEFAULT_LANGUAGE
import com.astrum.authentication.infrastructure.config.SYSTEM_ACCOUNT
import com.astrum.authentication.infrastructure.config.getCurrentUserLogin
import com.astrum.authentication.infrastructure.entities.Authority
import com.astrum.authentication.infrastructure.entities.User
import com.astrum.authentication.infrastructure.entities.dto.AdminUserDTO
import com.astrum.authentication.infrastructure.entities.dto.UserDTO
import com.astrum.authentication.infrastructure.repository.AuthorityRepository
import com.astrum.authentication.infrastructure.repository.UserRepository
import mu.KotlinLogging
import org.springframework.data.domain.Pageable
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant

/**
 * Service class for managing users.
 */
@Service
class UserService(
    private val userRepository: UserRepository,
    private val authorityRepository: AuthorityRepository
) {
    private val log = KotlinLogging.logger {}

    /**
     * Update basic information (first name, last name, email, language) for the current user.
     *
     * @param firstName first name of user.
     * @param lastName  last name of user.
     * @param email     email id of user.
     * @param langKey   language key.
     * @param imageUrl  image URL of user.
     * @return a completed {@link Mono}.
     */
    fun updateUser(
        firstName: String?,
        lastName: String?,
        email: String?,
        langKey: String?,
        imageUrl: String?
    ): Mono<Void> {
        return getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .flatMap {

                it.firstName = firstName
                it.lastName = lastName
                it.email = email?.lowercase()
                it.langKey = langKey
                it.imageUrl = imageUrl

                saveUser(it)
            }
            .doOnNext { log.debug("Changed Information for User: $it") }
            .then()
    }

    private fun saveUser(user: User): Mono<User> {
        return getCurrentUserLogin()
            .switchIfEmpty(Mono.just(SYSTEM_ACCOUNT))
            .flatMap { login ->
                if (user.createdBy == null) {
                    user.createdBy = login
                }
                user.lastModifiedBy = login
                userRepository.save(user)
            }
    }

    fun getAllManagedUsers(pageable: Pageable): Flux<AdminUserDTO> {
        return userRepository.findAllByIdNotNull(pageable).map { AdminUserDTO(it) }
    }

    fun getAllPublicUsers(pageable: Pageable): Flux<UserDTO> {
        return userRepository.findAllByIdNotNullAndActivatedIsTrue(pageable).map { UserDTO(it) }
    }

    fun countManagedUsers() = userRepository.count()

    fun getUserWithAuthoritiesByLogin(login: String): Mono<User> =
        userRepository.findOneByLogin(login)

    /**
     * @return a list of all the authorities
     */
    fun getAuthorities() =
        authorityRepository.findAll().mapNotNull(Authority::name)

    private fun syncUserWithIdP(details: Map<String, Any>, user: User): Mono<User> {
        // save authorities in to sync user roles/groups between IdP and JHipster's local database
        val userAuthorities = user.authorities.map { it.name }.toList()

        return getAuthorities().collectList()
            .flatMapMany { dbAuthorities: List<String?> ->
                val authoritiesToSave =
                    userAuthorities.filter { authority: String? -> !dbAuthorities.contains(authority) }
                        .map { authority: String? ->
                            val authorityToSave = Authority()
                            authorityToSave.name = authority
                            authorityToSave
                        }
                Flux.fromIterable(authoritiesToSave)
            }
            .doOnNext { authority: Authority? -> log.debug("Saving authority '$authority' in local database") }
            .flatMap { authorityRepository.save(it) }
            .then(userRepository.findOneByLogin(user.login!!))
            .switchIfEmpty(userRepository.save(user))
            .flatMap { existingUser ->
                // if IdP sends last updated information, use it to determine if an update should happen
                if (details["updated_at"] != null) {
                    val dbModifiedDate = existingUser.lastModifiedDate
                    val idpModifiedDate: Instant =
                        if (details["updated_at"] is Instant) details["updated_at"] as Instant else Instant.ofEpochSecond(
                            details["updated_at"] as Long
                        )
                    if (idpModifiedDate.isAfter(dbModifiedDate)) {
                        log.debug("Updating user '${user.login}' in local database")
                        return@flatMap updateUser(
                            user.firstName,
                            user.lastName,
                            user.email,
                            user.langKey,
                            user.imageUrl
                        )
                    }
                    // no last updated info, blindly update
                } else {
                    log.debug("Updating user '${user.login}' in local database")
                    return@flatMap updateUser(
                        user.firstName,
                        user.lastName,
                        user.email,
                        user.langKey,
                        user.imageUrl
                    )
                }
                return@flatMap Mono.empty<User>()
            }
            .thenReturn(user)
    }

    /**
     * Returns the user from an OAuth 2.0 login or resource server with JWT.
     * Synchronizes the user in the local repository.
     *
     * @param authToken the authentication token.
     * @return the user from the authentication.
     */
    fun getUserFromAuthentication(authToken: AbstractAuthenticationToken): Mono<AdminUserDTO> {
        val attributes: Map<String, Any> =
            when (authToken) {
                is OAuth2AuthenticationToken -> authToken.principal.attributes
                is JwtAuthenticationToken -> authToken.tokenAttributes
                else -> throw IllegalArgumentException("AuthenticationToken is not OAuth2 or JWT!")
            }

        val user = getUser(attributes)
        user.authorities = authToken.authorities.asSequence()
            .map(GrantedAuthority::getAuthority)
            .map { Authority(name = it) }
            .toMutableSet()
        return syncUserWithIdP(attributes, user).flatMap { Mono.just(AdminUserDTO(it)) }
    }

    companion object {

        @JvmStatic
        private fun getUser(details: Map<String, Any>): User {
            var activated = true
            val sub = details["sub"] as String
            val username = details["preferred_username"]?.let { it as String }
            val user = User()
            // handle resource server JWT, where sub claim is email and uid is ID
            if (details["uid"] != null) {
                user.id = details["uid"] as String
                user.login = sub
            } else {
                user.id = sub
            }
            if (username != null) {
                user.login = username.lowercase()
            } else if (user.login == null) {
                user.login = user.id
            }
            if (details["given_name"] != null) {
                user.firstName = details["given_name"] as String
            } else if (details["name"] != null) {
                user.firstName = details["name"] as String
            }
            if (details["family_name"] != null) {
                user.lastName = details["family_name"] as String
            }
            if (details["email_verified"] != null) {
                activated = details["email_verified"] as Boolean
            }
            if (details["email"] != null) {
                user.email = (details["email"] as String).lowercase()
            } else if (sub.contains("|") && (username != null && username.contains("@"))) {
                // special handling for Auth0
                user.email = username
            } else {
                user.email = sub
            }
            if (details["langKey"] != null) {
                user.langKey = details["langKey"] as String
            } else if (details["locale"] != null) {
                // trim off country code if it exists
                var locale = details["locale"] as String
                if (locale.contains("_")) {
                    locale = locale.substring(0, locale.indexOf("_"))
                } else if (locale.contains("-")) {
                    locale = locale.substring(0, locale.indexOf("-"))
                }
                user.langKey = locale.lowercase()
            } else {
                // set langKey to default if not specified by IdP
                user.langKey = DEFAULT_LANGUAGE
            }
            if (details["picture"] != null) {
                user.imageUrl = details["picture"] as String
            }
            user.activated = activated
            return user
        }
    }
}

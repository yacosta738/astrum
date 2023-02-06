package com.astrum.authentication.infrastructure

import com.astrum.authentication.infrastructure.config.SpringAddonsSecurityProperties
import com.astrum.authentication.infrastructure.config.SpringAddonsSecurityProperties.SimpleAuthoritiesMappingProperties
import com.astrum.authentication.infrastructure.domain.OAuth2AuthoritiesConverter
import com.astrum.authentication.infrastructure.exceptions.MissingAuthorizationServerConfigurationException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.JwtClaimNames
import java.util.*
import java.util.stream.Stream

class ConfigurableClaimSet2AuthoritiesConverter(
    private val properties: SpringAddonsSecurityProperties
) : OAuth2AuthoritiesConverter {


    private fun processCase(role: String, caze: SpringAddonsSecurityProperties.Case): String {
        return when (caze) {
            SpringAddonsSecurityProperties.Case.UPPER -> role.uppercase(Locale.getDefault())
            SpringAddonsSecurityProperties.Case.LOWER -> role.lowercase(Locale.getDefault())
            else -> role
        }
    }

    private fun getAuthoritiesMappingProperties(claimSet: Map<String, Any>): SimpleAuthoritiesMappingProperties {
        return properties.issuers
            .firstOrNull {
                it.location.toString() == claimSet[JwtClaimNames.ISS].toString()
            }
            ?.authorities
            ?: throw MissingAuthorizationServerConfigurationException(claimSet[JwtClaimNames.ISS].toString())
    }

    @Suppress("UNCHECKED_CAST")
    private fun getRoles(claims: Map<String, Any>, rolesPath: String): Stream<String> {
        val claimsToWalk = rolesPath.split("\\.".toRegex())
        var i = 0
        var obj = Optional.of(claims)
        while (i++ < claimsToWalk.size) {
            val claimName = claimsToWalk[i - 1]
            if (i == claimsToWalk.size) {
                return obj.map { it -> (it[claimName] as List<*>).stream().map { it.toString() } }
                    .orElse(Stream.empty())
            }
            obj = obj.map { it[claimName] as Map<String, Any> }
        }
        return Stream.empty()
    }

    /**
     * Convert the source object of type `S` to target type `T`.
     * @param source the source object to convert, which must be an instance of `S` (never `null`)
     * @return the converted object, which must be an instance of `T` (potentially `null`)
     * @throws IllegalArgumentException if the source cannot be converted to the desired target type
     */
    override fun convert(source: Map<String, Any>): Collection<GrantedAuthority> {
        val authoritiesMappingProperties = getAuthoritiesMappingProperties(source)
        return authoritiesMappingProperties.claims.flatMap { rolesPath ->
            getRoles(
                source,
                rolesPath
            ).toList()
        }
            .map { r ->
                "${authoritiesMappingProperties.prefix}${
                    processCase(
                        r,
                        authoritiesMappingProperties.caze
                    )
                }"
            }
            .map { r -> SimpleGrantedAuthority(r) }.toList()
    }
}

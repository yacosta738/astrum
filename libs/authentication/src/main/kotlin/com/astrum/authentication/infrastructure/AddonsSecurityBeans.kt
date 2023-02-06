package com.astrum.authentication.infrastructure

import com.astrum.authentication.infrastructure.config.SpringAddonsSecurityProperties
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority

val log = KotlinLogging.logger {}

@Configuration
@Import(SpringAddonsSecurityProperties::class)
class AddonsSecurityBeans {
    /**
     * Retrieves granted authorities from the Jwt (from its private claims or with the help of an external service)
     *
     * @param  securityProperties
     * @return
     */
    @ConditionalOnMissingBean
    @Bean
    fun authoritiesConverter(securityProperties: SpringAddonsSecurityProperties): Converter<Map<String, Any>, Collection<GrantedAuthority>> {
        log.debug("Building default CorsConfigurationSource with: {}", securityProperties)
        return ConfigurableClaimSet2AuthoritiesConverter(securityProperties)
    }
}

package com.astrum.authentication

import com.astrum.authentication.infrastructure.AddonsSecurityBeans
import com.astrum.authentication.infrastructure.AddonsWebSecurityBeans
import org.springframework.boot.autoconfigure.ImportAutoConfiguration


/**
 *
 *
 * Auto-configures [AddonsSecurityBeans] and [AddonsWebSecurityBeans]. To be used to test controllers but not services or
 * repositories (web context is not desired in that case).
 *
 * See [AutoConfigureAddonsSecurity]
 *
 * @author Yuniel Acosta
 */
@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@AutoConfigureAddonsSecurity
@ImportAutoConfiguration(AddonsWebSecurityBeans::class, AddonsWebfluxTestConf::class)
annotation class AutoConfigureAddonsWebSecurity

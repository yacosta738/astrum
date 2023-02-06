package com.astrum.authentication

import com.astrum.authentication.infrastructure.AddonsSecurityBeans
import com.astrum.authentication.infrastructure.config.SpringAddonsSecurityProperties
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ImportAutoConfiguration(SpringAddonsSecurityProperties::class, AddonsSecurityBeans::class)
@ExtendWith(SpringExtension::class)
annotation class AutoConfigureSecurityAddons

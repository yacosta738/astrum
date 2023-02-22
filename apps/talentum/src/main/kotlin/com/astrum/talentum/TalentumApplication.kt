package com.astrum.talentum

import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.properties.EnableConfigurationProperties


@SpringBootApplication(scanBasePackages = ["com.astrum.*"])
@EnableConfigurationProperties
class TalentumApplication

fun main(vararg args: String) {
    SpringApplicationBuilder(TalentumApplication::class.java)
        .web(WebApplicationType.REACTIVE)
        .run(*args)
}

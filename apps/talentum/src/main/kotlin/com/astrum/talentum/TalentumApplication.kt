package com.astrum.talentum

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication


@SpringBootApplication(scanBasePackages = ["com.astrum"])
@EnableConfigurationProperties
class TalentumApplication

fun main(vararg args: String) {
    runApplication<TalentumApplication>(*args)
}

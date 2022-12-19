package com.astrum.talentum

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TalentumApplication

fun main(vararg args: String) {
    runApplication<TalentumApplication>(*args)
}

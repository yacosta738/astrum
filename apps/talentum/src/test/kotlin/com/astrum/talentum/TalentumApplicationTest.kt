package com.astrum.talentum

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest
internal class TalentumApplicationTest {

    @Test
    @DisplayName("Check if context loads")
    fun contextLoads() {
        // Check if context loads
    }
}

package com.astrum.data.property

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "application.migration")
data class MigrationProperties(
    var clear: Boolean
)

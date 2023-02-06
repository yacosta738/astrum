package com.astrum.authentication

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties


@AutoConfiguration
@ConfigurationProperties(prefix = "com.astrum.springaddons.test.web")
class WebTestClientProperties {
    var defaultMediaType = "application/json"
    var defaultCharset = "utf-8"
}

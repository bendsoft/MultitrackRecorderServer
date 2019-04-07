package com.bendsoft.shared

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("mtr")
class ConfigurationProperties {
    lateinit var saveLocation: String
}

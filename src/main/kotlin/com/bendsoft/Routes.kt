package com.bendsoft

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.web.reactive.function.server.router

@Configuration
class Routes {
    @Bean
    fun router() = router {
        resources("/**", ClassPathResource("static/"))
    }
}

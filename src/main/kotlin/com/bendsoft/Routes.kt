package com.bendsoft

import com.bendsoft.channels.ChannelHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_EVENT_STREAM
import org.springframework.web.reactive.function.server.router

@Configuration
class Routes (
        private val channelHandler: ChannelHandler
) {
    @Bean
    fun router() = router {
        "/api".nest {
            "/channels".nest {
                accept(APPLICATION_JSON).nest {
                    GET("/", channelHandler::findAll)
                    GET("/{id}", channelHandler::findById)
                }
                accept(TEXT_EVENT_STREAM).nest {
                    GET("/stream", channelHandler::stream)
                }
            }
        }
        resources("/**", ClassPathResource("static/"))
    }
}

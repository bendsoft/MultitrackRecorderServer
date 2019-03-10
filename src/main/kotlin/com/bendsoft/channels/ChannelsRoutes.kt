package com.bendsoft.channels

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_EVENT_STREAM
import org.springframework.web.reactive.function.server.router

@Configuration
class ChannelsRoutes(
        private val channelHandler: ChannelHandler
) {
    @Bean
    fun channelsRouter() = router {
        "/api/channels".nest {
            accept(APPLICATION_JSON).nest {
                GET("/", channelHandler::findAll)
                POST("/", channelHandler::create)
                PUT("/{id}", channelHandler::update)
                DELETE("/{id}", channelHandler::delete)
                GET("/{id}", channelHandler::findById)
            }
            accept(TEXT_EVENT_STREAM).nest {
                GET("/stream", channelHandler::stream)
            }
        }
    }
}

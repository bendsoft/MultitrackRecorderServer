package com.bendsoft

import com.bendsoft.channels.ChannelHandler
import com.bendsoft.recordings.RecordingHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_EVENT_STREAM
import org.springframework.web.reactive.function.server.router

@Configuration
class Routes (
        private val channelHandler: ChannelHandler,
        private val recordingHandler: RecordingHandler
) {
    @Bean
    fun router() = router {
        "/api".nest {
            "/channels".nest {
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
            "/recordings".nest {
                accept(APPLICATION_JSON).nest {
                    GET("/", recordingHandler::findAll)
                    POST("/", recordingHandler::create)
                    GET("/{id}", recordingHandler::findById)
                    GET("/{date}", recordingHandler::findOnDate)
                    PUT("/{id}", recordingHandler::update)
                    DELETE("/{id}", recordingHandler::delete)
                    GET("/{id}/tracks", recordingHandler::findAllTracksOfRecording)
                    GET("/{id}/tracks/{trackId}", recordingHandler::findTrackInRecording)
                    POST("/{id}/track", recordingHandler::addTrackToRecording)
                    DELETE("/{id}/tracks/{trackId}", recordingHandler::deleteTrackFromRecording)
                }
                accept(TEXT_EVENT_STREAM).nest {
                    GET("/stream", recordingHandler::stream)
                }
            }
        }
        resources("/**", ClassPathResource("static/"))
    }
}

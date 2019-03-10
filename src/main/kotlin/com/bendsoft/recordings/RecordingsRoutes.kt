package com.bendsoft.recordings

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_EVENT_STREAM
import org.springframework.web.reactive.function.server.router

@Configuration
class RecordingsRoutes(
        private val recordingHandler: RecordingHandler,
        private val recorderHandler: RecorderHandler
) {
    @Bean
    fun recordingsRouter() = router {
        "/api/recordings".nest {
            accept(APPLICATION_JSON).nest {
                GET("/", recordingHandler::findAll)
                POST("/", recordingHandler::create)
                GET("/{id}", recordingHandler::findById)
                GET("/{id}/start", recorderHandler::start)
                POST("/{id}/stop", recorderHandler::stop)
                GET("/{id}/nextTrack", recorderHandler::nextTrack)
                GET("/{date}", recordingHandler::findOnDate)
                PUT("/{id}", recordingHandler::update)
                DELETE("/{id}", recordingHandler::delete)
                GET("/{id}/tracks", recordingHandler::findAllTracksOfRecording)
                GET("/{id}/tracks/{trackNumber}", recordingHandler::findTrackByTrackNumberInRecording)
                PUT("/{id}/track", recordingHandler::addTrackToRecording)
                PUT("/{id}/tracks/{trackNumber}", recordingHandler::removeTrackByTrackNumberFromRecording)
            }
            accept(TEXT_EVENT_STREAM).nest {
                GET("/stream", recordingHandler::stream)
            }
        }
    }
}

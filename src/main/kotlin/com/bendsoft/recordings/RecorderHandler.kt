package com.bendsoft.recordings

import com.bendsoft.channels.ChannelRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body

@Configuration
class RecorderHandler {
    @Autowired
    lateinit var recordingRepository: RecordingRepository
    @Autowired
    lateinit var channelsRepository: ChannelRepository

    fun start(req: ServerRequest) =
            ok().body(recordingRepository.findById(req.pathVariable("id")))
                    .switchIfEmpty(notFound().build())

    fun stop(req: ServerRequest) = ok()

    fun nextTrack(req: ServerRequest) = ok()
}

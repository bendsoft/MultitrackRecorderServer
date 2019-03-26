package com.bendsoft.recordings

import com.bendsoft.channels.ChannelRepository
import com.bendsoft.shared.MessageLevel
import com.bendsoft.shared.ErrorMessages
import com.bendsoft.shared.ServerResponseMessageFactory
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import javax.annotation.PostConstruct

@Configuration
class RecorderHandler {
    @Autowired
    private lateinit var recordingRepository: RecordingRepository
    @Autowired
    private lateinit var channelsRepository: ChannelRepository

    @Autowired
    private lateinit var recordingProcessFactory: RecordingProcessFactory
    private lateinit var recordingProcess: RecordingProcess

    private val logger = LoggerFactory.getLogger(RecorderHandler::class.java)

    @PostConstruct
    fun createRecordingProcessHandle() {
        recordingProcess = recordingProcessFactory.create()
    }

    fun processCommand(req: ServerRequest): Mono<ServerResponse> {
        if (recordingProcess.isRunning) {
            return ServerResponseMessageFactory.create(
                    ErrorMessages.RECORDING_IN_PROGRESS
            )
        }

        return recordingRepository.findById(req.pathVariable("id"))
                .zipWith(Mono.just(Track(
                        "Test",
                        42,
                        emptyList()
                )))
                .flatMap {
                    val trackAlreadyExists = it.t1.tracks.find { track -> track.trackNumber == it.t2.trackNumber }
                    if (trackAlreadyExists != null) {
                        ServerResponseMessageFactory.create(ErrorMessages.DUPLICATE_TRACK)
                    } else {
                        it.t1.copy(tracks = it.t1.tracks.plus(it.t2))

                        recordingRepository.save(it.t1)
                                .flatMap {
                                    channelsRepository
                                            .findAll()
                                            .collectList()
                                    )
                                    .flatMap { recordingChannelListTuple ->
                                    val emptyChannelRecordingFiles = recordingChannelListTuple.t2.

                                    val lastTrackCopy = recordingChannelListTuple.t1.tracks.last().copy(
                                            channelRecordingFiles = recordingChannelListTuple.t2
                                    )

                                    lastTrackCopy.channelRecordingFiles = it.t2

                                    logger.debug("before start RecordingProcess")

                                    recordingProcess.start(recording.tracks.last())

                                    logger.debug("after start RecordingProcess")

                                    ServerResponseMessageFactory.create(
                                            status = HttpStatus.OK,
                                            level = MessageLevel.INFO,
                                            message = "Recording process started",
                                            code = 1
                                    )
                                }
                                }
                    }
                }
    }

    fun start(req: ServerRequest): Mono<ServerResponse> {
        if (recordingProcess.isRunning) {
            return ServerResponseMessageFactory.create(
                    ErrorMessages.RECORDING_IN_PROGRESS
            )
        }

        return recordingRepository.findById(req.pathVariable("id"))
                .zipWith(req.bodyToMono(Track::class.java))
                .flatMap {
                    val trackAlreadyExists = it.t1.tracks.find { track -> track.trackNumber == it.t2.trackNumber }
                    if (trackAlreadyExists != null) {
                        ServerResponseMessageFactory.create(ErrorMessages.DUPLICATE_TRACK)
                    } else {
                        it.t1.copy(tracks = it.t1.tracks.plus(it.t2))
                        recordingRepository.save(it.t1)
                                .flatMap {recording ->
                                    logger.info("before start RecordingProcess")
                                    recordingProcess.start(recording.tracks.last())
                                    logger.info("after start RecordingProcess")

                                    ServerResponseMessageFactory.create(
                                            status = HttpStatus.OK,
                                            level = MessageLevel.INFO,
                                            message = "Recording process started",
                                            code = 1
                                    )
                                }
                    }
                }
    }

    fun stop(req: ServerRequest): Mono<ServerResponse> {
        if (!recordingProcess.isRunning) {
            return ServerResponseMessageFactory.create(ErrorMessages.NO_RECORDING_IN_PROGRESS)
        }

        recordingProcess.stop()

        return ok().build()
    }

    fun nextTrack(req: ServerRequest): Mono<ServerResponse> =
            stop(req)
                    .flatMap {
                        if (it.statusCode().is2xxSuccessful) {
                            start(req)
                        } else {
                            it.toMono()
                        }
                    }
}

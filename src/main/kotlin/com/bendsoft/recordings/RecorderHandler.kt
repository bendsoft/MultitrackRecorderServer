package com.bendsoft.recordings

import com.bendsoft.channelrecordingfiles.ChannelRecordingFile
import com.bendsoft.channels.ChannelRepository
import com.bendsoft.recordings.recorder.RecordingProcess
import com.bendsoft.recordings.recorder.TracksRecorder
import com.bendsoft.shared.ErrorMessages
import com.bendsoft.shared.MessageLevel
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
    private lateinit var tracksRecorder: TracksRecorder
    private lateinit var recordingProcess: RecordingProcess

    private val logger = LoggerFactory.getLogger(RecorderHandler::class.java)

    @PostConstruct
    fun createRecordingProcessHandle() {
        tracksRecorder.checkAvailableMixers()
        recordingProcess = tracksRecorder.create()
    }

    fun processCommand(req: ServerRequest): Mono<ServerResponse> {
        if (recordingProcess.isRunning) {
            return ServerResponseMessageFactory.create(
                ErrorMessages.RECORDING_IN_PROGRESS
            )
        }

        return recordingRepository.findById(req.pathVariable("id"))
            .zipWith(
                enrichTrackWithChannelRecordingFiles(
                    Track("Test", 42, emptyList())
                )
            )
            .flatMap {
                val recording = it.t1
                val newTrack = it.t2

                if (isTrackDuplicate(recording, newTrack)) {
                    ServerResponseMessageFactory.create(ErrorMessages.DUPLICATE_TRACK)
                } else {
                    recording.tracks.plus(newTrack)

                    recordingRepository.save(it.t1)
                        .flatMap {
                            logger.debug("before start RecordingProcess")

                            recordingProcess.start(newTrack)

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

    private fun isTrackDuplicate(recording: Recording, newTrack: Track) =
        recording.tracks.find { track -> track.trackNumber == newTrack.trackNumber } != null

    private fun enrichTrackWithChannelRecordingFiles(newTrack: Track): Mono<Track> {
        return channelsRepository
            .findAll()
            .map {
                ChannelRecordingFile(
                    filename = "${newTrack.name}_${it.name}.wav",
                    channelName = it.name,
                    channelNumber = it.channelNumber,
                    data = byteArrayOf()
                )
            }
            .collectList()
            .map {
                newTrack.copy(
                    channelRecordingFiles = it
                )
            }
    }

    fun start(req: ServerRequest): Mono<ServerResponse> {
        if (recordingProcess.isRunning) {
            return ServerResponseMessageFactory.create(
                ErrorMessages.RECORDING_IN_PROGRESS
            )
        }

        return recordingRepository.findById(req.pathVariable("id"))
            .zipWhen {
                req.bodyToMono(Track::class.java)
                    .doOnNext {
                        enrichTrackWithChannelRecordingFiles(it)
                    }
            }
            .flatMap {
                val recording = it.t1
                val newTrack = it.t2

                if (isTrackDuplicate(recording, newTrack)) {
                    ServerResponseMessageFactory.create(ErrorMessages.DUPLICATE_TRACK)
                } else {
                    recording.tracks.plus(newTrack)

                    recordingRepository.save(recording)
                        .flatMap {
                            logger.debug("before start RecordingProcess")

                            recordingProcess.start(newTrack)

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

    fun stop(req: ServerRequest): Mono<ServerResponse> {
        if (!recordingProcess.isRunning) {
            return ServerResponseMessageFactory.create(ErrorMessages.NO_RECORDING_IN_PROGRESS)
        }

        recordingProcess.stop()

        return recordingRepository.findById(req.pathVariable("id"))
            .flatMap {
                it.tracks.last().channelRecordingFiles.map { channelRecordingFile ->
                    channelRecordingFile.copy(
                        data = byteArrayOf()
                    )
                }
                recordingRepository.save(it)

                ok().build()
            }
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

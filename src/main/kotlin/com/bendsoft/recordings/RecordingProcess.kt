package com.bendsoft.recordings

import com.bendsoft.channelrecordingfiles.ChannelRecordingFile
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
class AsyncTrackRecorder {
    private val logger = LoggerFactory.getLogger(AsyncTrackRecorder::class.java)

    @Async
    fun recordTrack(runningRecording: Process, track: Track): CompletableFuture<Track> {
        logger.debug("before bufferReader defined")

        runningRecording
            .inputStream
            .bufferedReader()
            .lines()
            .forEach { println(it) }

        logger.debug("after bufferReader defined")

        return CompletableFuture.completedFuture(
                Track(
                        track.name,
                        track.trackNumber,
                        getChannelFiles()
                )
        )
    }

    private fun getChannelFiles(): List<ChannelRecordingFile> {

    }
}

interface RecordingProcess {
    var isRunning: Boolean
    fun start(track: Track)
    fun stop(): Track?
}

@Component
class RecordingProcessFactory {
    private val logger = LoggerFactory.getLogger(RecordingProcess::class.java)

    @Autowired
    private lateinit var trackRecorder: AsyncTrackRecorder

    fun create() = object : RecordingProcess {
        override var isRunning = false
        private var track: Track? = null

        private lateinit var runningRecording: Process

        override fun start(track: Track) {
            this.track = track

            logger.info("run recording-process")
            isRunning = true

            runningRecording = ProcessBuilder()
                    .command(listOf("ping", "192.168.1.1", "-t"))
                    .start()

            trackRecorder.recordTrack(runningRecording, track)
            logger.info("Process started")
        }

        override fun stop(): Track? {
            logger.info("stop called")
            if (isRunning) {
                runningRecording.destroy()
                isRunning = false
                logger.info("process has been stopped")
            }

            return track
        }
    }
}
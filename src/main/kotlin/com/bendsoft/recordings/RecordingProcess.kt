package com.bendsoft.recordings

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class AsyncTrackRecorder {
    private val logger = LoggerFactory.getLogger(AsyncTrackRecorder::class.java)

    @Async
    fun recordTrack(runningRecording: Process, track: Track) {
        logger.debug("before bufferReader defined")

        runningRecording
            .inputStream
            .bufferedReader()
            .lines()
            .forEach { println(it) }

        logger.debug("after bufferReader defined")
    }
}

interface RecordingProcess {
    var isRunning: Boolean
    fun start(track: Track)
    fun stop()
}

@Component
class RecordingProcessFactory {
    private val logger = LoggerFactory.getLogger(RecordingProcess::class.java)

    @Autowired
    private lateinit var trackRecorder: AsyncTrackRecorder

    fun create() = object : RecordingProcess {
        override var isRunning = false

        private lateinit var runningRecording: Process

        override fun start(track: Track) {
            logger.info("run recording-process")
            isRunning = true

            runningRecording = ProcessBuilder()
                    .command(listOf("ping", "192.168.1.1", "-t"))
                    .start()

            trackRecorder.recordTrack(runningRecording, track)
            logger.info("Process started")
        }

        override fun stop() {
            logger.info("stop called")
            if (isRunning) {
                runningRecording.destroy()
                isRunning = false
                logger.info("process has been stopped")
            }
        }
    }
}

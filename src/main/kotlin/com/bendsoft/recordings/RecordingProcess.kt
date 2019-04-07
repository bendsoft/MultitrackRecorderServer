package com.bendsoft.recordings

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream
import java.io.File
import javax.sound.sampled.*

@Component
class AsyncTrackRecorder {
    private val logger = LoggerFactory.getLogger(AsyncTrackRecorder::class.java)

    @Value("\${mtr.save-location}")
    private lateinit var saveLocation: String

    @Async
    fun recordTrack(recordingProcess: RecordingProcess, track: Track) {
        val audioFormat = TracksRecorder.getAudioFormat()
        val channelBuffersFileStream: Map<Int, Pair<ByteArrayOutputStream, AudioInputStream>> =
            track.channelRecordingFiles.map {
                it.channelNumber to Pair(
                    ByteArrayOutputStream(),
                    AudioSystem.getAudioInputStream(
                        File("$saveLocation/${it.filename}")
                    )
                )
            }.toMap()

        val buffer = ByteArray(audioFormat.frameSize)

        logger.debug("before read loop defined")

        while (recordingProcess.isRunning) {
            recordingProcess.audioLine.read(buffer, 0, buffer.size)

            deinterleaveBuffer(buffer)
                .forEachIndexed { index, channelBytes ->
                    if (channelBuffersFileStream.containsKey(index)) {
                        val currentBufferStreamPair = channelBuffersFileStream.getValue(index)

                        currentBufferStreamPair.first.write(channelBytes)
                        if (currentBufferStreamPair.first.size() >= TracksRecorder.BUFFER_SIZE) {
                            writeBufferToFile(currentBufferStreamPair)
                        }
                    }
                }
        }

        logger.debug("before read loop defined")

        channelBuffersFileStream.forEach {
            writeBufferToFile(it.value)
        }
    }

    private fun deinterleaveBuffer(buffer: ByteArray): List<ByteArray> {
        return buffer.toList()
            .chunked(TracksRecorder.getAudioFormat().channels)
            .map { it.toByteArray() }
    }

    private fun writeBufferToFile(currentBufferStreamPair: Pair<ByteArrayOutputStream, AudioInputStream>) {
        AudioSystem.write(currentBufferStreamPair.second, TracksRecorder.FILE_TYPE, currentBufferStreamPair.first)
    }
}

interface RecordingProcess {
    var isRunning: Boolean
    val audioLine: TargetDataLine
    fun start(track: Track)
    fun stop()
}

@Component
class TracksRecorder {
    companion object {
        const val BUFFER_SIZE = 4096
        val FILE_TYPE: AudioFileFormat.Type = AudioFileFormat.Type.WAVE

        fun getAudioFormat(): AudioFormat {
            val sampleRate = 48000f
            val sampleSizeInBits = 24
            val channels = 16
            val signed = true
            val bigEndian = false
            return AudioFormat(
                sampleRate, sampleSizeInBits, channels, signed, bigEndian
            )
        }
    }

    private val logger = LoggerFactory.getLogger(RecordingProcess::class.java)

    @Autowired
    private lateinit var trackRecorder: AsyncTrackRecorder

    fun create() = object : RecordingProcess {
        override var isRunning = false

        val format: AudioFormat = TracksRecorder.getAudioFormat()
        override val audioLine: TargetDataLine = AudioSystem.getTargetDataLine(format)

        override fun start(track: Track) {
            logger.info("run recording-process")
            isRunning = true

            val info = DataLine.Info(TargetDataLine::class.java, format)

            // checks if system supports the data line
            if (!AudioSystem.isLineSupported(info)) {
                throw LineUnavailableException(
                    "The system does not support the specified format.")
            }

            audioLine.open(format)
            audioLine.start()

            trackRecorder.recordTrack(this, track)

            logger.info("Process started")
        }

        override fun stop() {
            logger.info("stop called")
            if (isRunning) {
                isRunning = false
                logger.info("process has been stopped")
            }
        }
    }
}

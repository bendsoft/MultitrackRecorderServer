package com.bendsoft.recordings

import com.bendsoft.shared.MtrProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream
import java.io.File
import javax.sound.sampled.*
import javax.sound.sampled.AudioSystem.getMixerInfo

@Component
class AsyncTrackRecorder {
    private val logger = LoggerFactory.getLogger(AsyncTrackRecorder::class.java)

    @Autowired
    private lateinit var properties: MtrProperties

    @Async
    fun recordTrack(recordingProcess: RecordingProcess, track: Track, audioFormat: AudioFormat) {
        if (properties.recorder.saveLocation == null) {
            throw NullPointerException("Property mtr.recorder.saveLocation must be set")
        }

        val channelBuffersFileStream: Map<Int, Pair<ByteArrayOutputStream, AudioInputStream>> =
            track.channelRecordingFiles.map {
                it.channelNumber to Pair(
                    ByteArrayOutputStream(),
                    AudioSystem.getAudioInputStream(
                        File("${properties.recorder.saveLocation}/${it.filename}")
                    )
                )
            }.toMap()

        val buffer = ByteArray(audioFormat.frameSize)

        logger.debug("before read loop defined")

        var fileInputStream: AudioInputStream
        var audioOutputStream = ByteArrayOutputStream()
        val isMaxBufferSizeExceeded: (ByteArray) -> Boolean =
            { bytesToAdd -> audioOutputStream.size() + bytesToAdd.size >= properties.recorder.bufferSize }

        while (recordingProcess.isRunning) {
            recordingProcess.audioLine.read(buffer, 0, buffer.size)

            deinterleaveBuffer(buffer, audioFormat)
                .forEachIndexed { index, channelBytes ->
                    if (channelBuffersFileStream.containsKey(index)) {
                        audioOutputStream = channelBuffersFileStream.getValue(index).first
                        fileInputStream = channelBuffersFileStream.getValue(index).second

                        if (isMaxBufferSizeExceeded(channelBytes)) {
                            writeBufferToFile(audioOutputStream, fileInputStream)
                        }
                        audioOutputStream.write(channelBytes)
                    }
                }
        }

        logger.debug("before read loop defined")

        channelBuffersFileStream.forEach {
            writeBufferToFile(it.value.first, it.value.second)
        }
    }

    private fun deinterleaveBuffer(buffer: ByteArray, audioFormat: AudioFormat): List<ByteArray> {
        return buffer.toList()
            .chunked(audioFormat.channels)
            .map { it.toByteArray() }
    }

    @Async
    fun writeBufferToFile(buffer: ByteArrayOutputStream, fileInputStream: AudioInputStream) {
        AudioSystem.write(fileInputStream, properties.recorder.fileType, buffer)
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
    private val logger = LoggerFactory.getLogger(RecordingProcess::class.java)

    @Autowired
    private lateinit var trackRecorder: AsyncTrackRecorder

    @Autowired
    private lateinit var properties: MtrProperties

    fun create() = object : RecordingProcess {
        override var isRunning = false

        val format: AudioFormat = getAudioFormat()
        override val audioLine: TargetDataLine = AudioSystem.getTargetDataLine(format)

        override fun start(track: Track) {
            isRunning = true

            logger.info("available mixers")
            getMixerInfo().forEach {
                logger.info(it.toString())
            }

            val info = DataLine.Info(TargetDataLine::class.java, format)

            // checks if system supports the data line
            if (!AudioSystem.isLineSupported(info)) {
                throw LineUnavailableException(
                    "The system does not support the specified format.")
            }

            audioLine.open(format)
            audioLine.start()

            logger.info("run recording-process")
            trackRecorder.recordTrack(this, track, format)

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

    private fun getAudioFormat(): AudioFormat {
        return AudioFormat(
            properties.recorder.sampleRate,
            properties.recorder.sampleSizeInBits,
            properties.recorder.channels,
            properties.recorder.signed,
            properties.recorder.bigEndian
        )
    }
}

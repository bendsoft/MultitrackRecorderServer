package com.bendsoft.recordings.recorder

import com.bendsoft.recordings.Track
import com.bendsoft.shared.MtrProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream
import java.io.File
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

@Component
class AsyncTrackRecorder {
    private val logger = LoggerFactory.getLogger(AsyncTrackRecorder::class.java)

    @Autowired
    private lateinit var properties: MtrProperties

    private lateinit var audioFormat: AudioFormat

    @Async
    fun recordTrack(recordingProcess: RecordingProcess, track: Track, audioFormat: AudioFormat) {
        if (properties.recorder.saveLocation == null) {
            throw NullPointerException("Property mtr.recorder.saveLocation must be set")
        }

        this.audioFormat = audioFormat

        val channelsToRecord = track.channelRecordingFiles.map { it.channelNumber }

        val channelBuffers: Map<Int, ByteArrayOutputStream> =
            channelsToRecord.map {
                it to ByteArrayOutputStream()
            }.toMap()

        val channelFileStreams: Map<Int, AudioInputStream> =
            track.channelRecordingFiles.map {
                it.channelNumber to AudioSystem.getAudioInputStream(
                    File("${properties.recorder.saveLocation}/${it.filename}")
                )
            }.toMap()

        val frameBufferSizeMultiplier = 10
        val frameBuffer = ByteArray(audioFormat.frameSize * frameBufferSizeMultiplier)

        logger.debug("before read loop defined")

        var audioOutputStream: ByteArrayOutputStream

        var frameList: List<Frame>
        var channelData: ByteArray

        recordingProcess.audioLine.start()
        while (recordingProcess.isRunning) {
            recordingProcess.audioLine.read(frameBuffer, 0, frameBuffer.size)

            frameList = convertToFrames(frameBuffer)

            channelsToRecord.forEach {
                audioOutputStream = channelBuffers.getValue(it)

                channelData = frameList
                    .flatMap { frame -> frame.getSample(it) }
                    .toByteArray()

                if (willMaxBufferSizeBeExceeded(audioOutputStream, channelData)) {
                    writeBufferToFile(audioOutputStream, channelFileStreams.getValue(it))
                }
                audioOutputStream.write(channelData)
            }
        }

        logger.debug("before read loop defined")

        channelBuffers.forEach {
            writeBufferToFile(it.value, channelFileStreams.getValue(it.key))
        }
    }

    private fun willMaxBufferSizeBeExceeded(audioOutputStream: ByteArrayOutputStream, bytesToAdd: ByteArray) =
        audioOutputStream.size() + bytesToAdd.size >= properties.recorder.bufferSize

    private fun convertToFrames(buffer: ByteArray): List<Frame> {
        return buffer.toList()
            .chunked(audioFormat.frameSize)
            .map { Frame(it, audioFormat.channels) }
    }

    @Async
    fun writeBufferToFile(buffer: ByteArrayOutputStream, fileInputStream: AudioInputStream) {
        AudioSystem.write(fileInputStream, properties.recorder.fileType, buffer)
    }
}

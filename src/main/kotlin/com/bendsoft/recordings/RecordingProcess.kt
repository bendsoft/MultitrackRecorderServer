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

    private val frameSizeBufferMultiplier = 10
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

        val buffer = ByteArray(audioFormat.frameSize * frameSizeBufferMultiplier)

        logger.debug("before read loop defined")

        var audioOutputStream: ByteArrayOutputStream

        var frameList: List<Frame>
        var channelData: ByteArray

        recordingProcess.audioLine.start()
        while (recordingProcess.isRunning) {
            recordingProcess.audioLine.read(buffer, 0, buffer.size)

            frameList = convertToFrames(buffer, audioFormat)

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

    private fun convertToFrames(buffer: ByteArray, audioFormat: AudioFormat): List<Frame> {
        return buffer.toList()
            .chunked(audioFormat.frameSize)
            .map { Frame(it, audioFormat.channels) }
    }

    @Async
    fun writeBufferToFile(buffer: ByteArrayOutputStream, fileInputStream: AudioInputStream) {
        AudioSystem.write(fileInputStream, properties.recorder.fileType, buffer)
    }
}

class Frame(
    frameByteList: List<Byte>,
    channels: Int
) {
    val samples = frameByteList
        .chunked(channels)

    fun getSample(channelNumber: Int) = samples[channelNumber]
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
            audioLine.addLineListener {
                if (it.type === LineEvent.Type.STOP || it.type === LineEvent.Type.CLOSE) {
                    stop()
                }
            }

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

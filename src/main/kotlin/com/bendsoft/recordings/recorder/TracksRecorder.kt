package com.bendsoft.recordings.recorder

import com.bendsoft.recordings.Track
import com.bendsoft.shared.MtrProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.sound.sampled.*

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
            AudioSystem.getMixerInfo().forEach {
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

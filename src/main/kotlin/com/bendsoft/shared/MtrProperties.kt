package com.bendsoft.shared

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import javax.sound.sampled.AudioFileFormat

@Configuration
@ConfigurationProperties("mtr")
class MtrProperties {

    val recorder = Recorder()

    class Recorder {
        var saveLocation: String? = null
        var sampleRate: Float = 48000f
        var sampleSizeInBits: Int = 24
        var channels: Int = 16
        var signed: Boolean = true
        var bigEndian: Boolean = false
        var bufferSize = 4096
        var fileType: AudioFileFormat.Type = AudioFileFormat.Type.WAVE
    }
}

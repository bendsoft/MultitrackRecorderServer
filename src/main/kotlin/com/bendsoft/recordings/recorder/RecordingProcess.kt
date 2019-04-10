package com.bendsoft.recordings.recorder

import com.bendsoft.recordings.Track
import javax.sound.sampled.TargetDataLine

interface RecordingProcess {
    var isRunning: Boolean
    val audioLine: TargetDataLine
    fun start(track: Track)
    fun stop()
}

package com.bendsoft.recordings.recorder

class Frame(
    frameByteList: List<Byte>,
    channels: Int
) {
    val samples = frameByteList
        .chunked(channels)

    fun getSample(channelNumber: Int) = samples[channelNumber]
}

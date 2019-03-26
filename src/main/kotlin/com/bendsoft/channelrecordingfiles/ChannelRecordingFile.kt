package com.bendsoft.channelrecordingfiles

data class ChannelRecordingFile (
        val filename: String,
        val channelName: String,
        val channelNumber: Int,
        val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChannelRecordingFile

        if (filename != other.filename) return false
        if (channelName != other.channelName) return false
        if (channelNumber != other.channelNumber) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = filename.hashCode()
        result = 31 * result + channelName.hashCode()
        result = 31 * result + channelNumber
        result = 31 * result + data.contentHashCode()
        return result
    }
}
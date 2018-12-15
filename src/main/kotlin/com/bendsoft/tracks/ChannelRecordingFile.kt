package com.bendsoft.tracks

import org.springframework.data.annotation.Id

data class ChannelRecordingFile (
        @Id val id: String? = null,
        val filename: String,
        val name: String,
        val channelNumber: Int,
        val type: String,
        val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChannelRecordingFile

        if (id != other.id) return false
        if (filename != other.filename) return false
        if (name != other.name) return false
        if (channelNumber != other.channelNumber) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + filename.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + channelNumber
        result = 31 * result + type.hashCode()
        return result
    }
}

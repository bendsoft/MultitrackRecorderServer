package com.bendsoft.recordings

import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection="tracks")
@TypeAlias("model.track")
data class Track (
        val name: String = "",
        val trackNumber: Int,
        val channelRecordingFiles: List<ChannelRecordingFile> = emptyList()
)

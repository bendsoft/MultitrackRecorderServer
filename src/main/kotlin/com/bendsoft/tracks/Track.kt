package com.bendsoft.tracks

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection="tracks")
@TypeAlias("model.track")
data class Track (
        @Id val id: String? = null,
        val name: String = "",
        val trackNumber: Int,
        val channelRecordingFiles: List<ChannelRecordingFile> = emptyList()
)

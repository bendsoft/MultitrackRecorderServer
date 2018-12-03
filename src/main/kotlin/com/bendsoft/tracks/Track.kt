package com.bendsoft.tracks

import com.bendsoft.channelrecordingfile.ChannelRecordingFile
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection="tracks")
@TypeAlias("model.track")
data class Track (
        @Id val id: String? = null,
        val name: String = "",
        val trackNumber: Int,
        val channels: List<ChannelRecordingFile> = emptyList()
)

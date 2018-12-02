package com.bendsoft.channelrecordingfile

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection="channelRecordingFiles")
@TypeAlias("model.channelRecordingFile")
data class ChannelRecordingFile (
        @Id val id: String? = null,
        val name: String = "",
        val channelNumber: Int,
        val size: Int,
        val data: Byte
)

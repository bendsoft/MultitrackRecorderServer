package com.bendsoft.model.track

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection="tracks")
@TypeAlias("recording.track")
data class Track (
        @Id val id: String? = null,
        val name: String = ""
)

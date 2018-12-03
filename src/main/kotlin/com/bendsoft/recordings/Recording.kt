package com.bendsoft.recordings

import com.bendsoft.tracks.Track
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate

@Document(collection="recordings")
@TypeAlias("model.recording")
data class Recording (
        @Id val id: String? = null,
        val name: String = "",
        val recordingDate: LocalDate,
        val track: List<Track> = emptyList()
)

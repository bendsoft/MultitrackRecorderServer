package com.bendsoft.channels

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection="channels")
@TypeAlias("model.channel")
data class Channel (
        @Id val id: String? = null,
        val name: String = "",
        val selectedChannel: Int,
        val active: Boolean
)

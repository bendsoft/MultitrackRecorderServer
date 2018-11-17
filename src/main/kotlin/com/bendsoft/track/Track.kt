package com.bendsoft.track

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
class Track(@Id val id: String? = null, val name: String = "") {
}

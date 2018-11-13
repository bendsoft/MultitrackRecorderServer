package ch.ideadapt.springreactive.track

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
class Track(@Id val id: String?, val name: String = "") {
}

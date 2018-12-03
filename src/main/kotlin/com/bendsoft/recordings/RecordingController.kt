package com.bendsoft.recordings

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@RestController
@RequestMapping("/reactive/recordings")
class RecordingController {

	@Autowired
	lateinit var repository: RecordingRepository

	@GetMapping("/")
	fun getRecordings(): Flux<Recording> {
		return repository.findAll();
	}

	@GetMapping("/{id}")
	fun getRecording(
			@PathVariable("id") id: String
	): Mono<Recording> {
		return repository.findById(id)
	}

	@GetMapping(
            path = ["/"],
			params = ["recordingDate"]
	)
	fun findRecording(
			@RequestParam("recordingDate") recordingDate: String
	): Flux<Recording> {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMAN)
        return repository.findAllRecordingOnDate(LocalDate.parse(recordingDate, formatter))
	}
}

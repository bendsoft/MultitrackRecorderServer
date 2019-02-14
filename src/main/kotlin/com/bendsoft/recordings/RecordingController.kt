package com.bendsoft.recordings

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping("/recordings")
class RecordingController {

	@Autowired
	lateinit var repository: RecordingRepository

	@GetMapping("/")
	fun getRecordings(): Flux<Recording> {
		return repository.findAll()
	}

	@GetMapping("/{recordingId}")
	fun getRecording(
			@PathVariable("recordingId") recordingId: String
	): Mono<Recording> {
		return repository.findById(recordingId)
	}

    @PutMapping("/")
    fun updateRecording(
            @Valid @RequestBody recording: Recording
    ): Mono<Recording> {
        return repository.save(recording)
    }

    @PostMapping("/")
    fun createRecording(
            @Valid @RequestBody recording: Recording
    ): Mono<Recording> {
        return repository.save(recording)
    }

    @DeleteMapping("/{recordingId}")
    fun deleteRecording(
            @PathVariable("recordingId") recordingId: String
    ): Mono<Void> {
        return repository.deleteById(recordingId)
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

    @GetMapping("/{recordingId}/tracks")
    fun getTracks(
            @PathVariable("recordingId") recordingId: String
    ): Mono<List<Track>> {
        return repository.findById(recordingId)
                .map { it.tracks }
    }

    @PostMapping("/{recordingId}/track")
    fun createTrack(
            @PathVariable("recordingId") recordingId: String,
            @Valid @RequestBody track: Track
    ): Mono<Recording> {
        return repository.findById(recordingId)
                .doOnNext { it.tracks.plus(track) }
                .doOnNext { repository.save(it) }
    }

    @DeleteMapping("/{recordingId}/track/{trackId}")
    fun deleteTrack(
            @PathVariable("recordingId") recordingId: String,
            @PathVariable("trackId") trackId: String
    ): Mono<Recording> {
        return repository.findById(recordingId)
                .doOnNext { it.tracks.dropWhile { track -> track.id.equals(trackId) } }
                .doOnNext { repository.save(it) }
    }
}

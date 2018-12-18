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
		return repository.findAll();
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
    ): Flux<Track> {
        return repository.findTrackById(recordingId)
    }

    @GetMapping("/{recordingId}/track/{trackId}")
    fun getTrack(
            @PathVariable("recordingId") recordingId: String,
            @PathVariable("trackId") trackId: String
    ): Mono<Track> {
        return repository.findAllTracks(recordingId, trackId)
    }

    @PostMapping("/{recordingId}/track")
    fun createTrack(
            @PathVariable("recordingId") recordingId: String,
            @Valid @RequestBody track: Track
    ): Mono<Recording> {
        return repository.saveTrack(recordingId, track)
    }

    @DeleteMapping("/{recordingId}/track/{trackId}")
    fun deleteTrack(
            @PathVariable("recordingId") recordingId: String,
            @PathVariable("trackId") trackId: String
    ): Mono<Void> {
        return repository.deleteTrackById(recordingId, trackId)
    }
}

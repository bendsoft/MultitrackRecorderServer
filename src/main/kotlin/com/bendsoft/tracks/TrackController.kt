package com.bendsoft.tracks

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/reactive/tracks")
class TrackController {

	@Autowired
	lateinit var repository: TrackRepository

	@GetMapping("/")
	fun getTracks(): Flux<Track> {
		return repository.findAll();
	}

	@GetMapping("/{id}")
	fun getTrack(@PathVariable("id") id: String): Mono<Track> {
		return repository.findById(id)
	}
}

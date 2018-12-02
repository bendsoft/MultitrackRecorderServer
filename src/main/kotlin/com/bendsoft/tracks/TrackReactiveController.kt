package com.bendsoft.tracks

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/reactive/tracks")
class TrackReactiveController {

	@Autowired
	lateinit var repository: TrackReactiveRepository

	@RequestMapping(
			method = [RequestMethod.GET])
	fun getTracks(): Flux<Track> {
		return repository.findAll();
	}

	@RequestMapping(
			"/{id}",
			method = [RequestMethod.GET])
	fun getTrack(@PathVariable("id") id: String): Mono<Track> {
		return repository.findById(id)
	}
}

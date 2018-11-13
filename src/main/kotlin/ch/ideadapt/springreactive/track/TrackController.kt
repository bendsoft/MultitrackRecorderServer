package ch.ideadapt.springreactive.track

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/tracks")
class TrackController {

	@Autowired
	lateinit var repository: TrackRepository

	@RequestMapping(
			method = [RequestMethod.GET])
	fun getTracks(): List<Track> {
		return repository.findAll().toList();
	}

	@RequestMapping(
			"/{id}",
			method = [RequestMethod.GET])
	fun getTrack(@PathVariable("id") id: String): Optional<Track> {
		return repository.findById(id);
	}
}


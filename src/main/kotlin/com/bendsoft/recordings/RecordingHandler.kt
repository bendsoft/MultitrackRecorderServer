package com.bendsoft.recordings

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.*
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.bodyToServerSentEvents
import reactor.core.publisher.Mono
import java.net.URI
import java.time.LocalDate

@Configuration
class RecordingHandler {
    @Autowired
    lateinit var repository: RecordingRepository

    fun findAll(req: ServerRequest) =
            ok().body(repository.findAll())
                    .switchIfEmpty(notFound().build())

    fun findById(req: ServerRequest) =
            ok().body(repository.findById(req.pathVariable("id")))

    fun findOnDate(req: ServerRequest) =
            ok().body(repository.findAllRecordingOnDate(
                    LocalDate.parse(req.pathVariable("date"))
            ))

    fun stream(req: ServerRequest) =
            ok().bodyToServerSentEvents(repository.findAll())

    fun create(req: ServerRequest) =
            ok().body(req.bodyToMono(Recording::class.java)
                    .doOnNext { repository.save(it) }
                    .doOnNext { created(URI.create("/recordings/$it")).build() })

    fun update(req: ServerRequest) =
            ok().body(repository.findById(req.pathVariable("id"))
                    .zipWith(req.bodyToMono(Recording::class.java))
                    .map {
                        it.t1.copy(
                                name = it.t2.name,
                                recordingDate = it.t2.recordingDate,
                                tracks = it.t1.tracks.union(it.t2.tracks).toList()
                        )
                    }
                    .doOnNext { repository.save(it) }
                    .doOnNext { ok().build() })

    fun delete(req: ServerRequest) =
            ok().body(repository.deleteById(req.pathVariable("id"))
                    .flatMap { noContent().build() })

    fun findAllTracksOfRecording(req: ServerRequest) =
            ok().body(repository.findById(req.pathVariable("id"))
                    .map { it.tracks })

    private fun compareTrackToPathVariable(track: Track, req: ServerRequest) =
            track.trackNumber == req.pathVariable("trackNumber").toInt()

    fun findTrackByTrackNumberInRecording(req: ServerRequest) =
            repository.findById(req.pathVariable("id"))
                    .map { it.tracks.find { track -> compareTrackToPathVariable(track, req) } }
                    .flatMap { ok().body(Mono.justOrEmpty(it), Track::class.java) }
                    .switchIfEmpty(notFound().build())

    fun addTrackToRecording(req: ServerRequest) =
            repository.findById(req.pathVariable("id"))
                    .zipWith(req.bodyToMono(Track::class.java))
                    .flatMap {
                        val trackAlreadyExists = it.t1.tracks.find { track -> track.trackNumber == it.t2.trackNumber }
                        if (trackAlreadyExists != null)
                            unprocessableEntity().body(BodyInserters.fromObject(
                                    mapOf(
                                            "level" to "ERROR",
                                            "message" to "Recording already has a track with number ${trackAlreadyExists.trackNumber}",
                                            "code" to -1,
                                            "entity" to it.t2
                                    )
                            ))
                        else {
                            it.t1.tracks.plus(it.t2)
                            repository.save(it.t1)
                            ok().build()
                        }
                    }

    fun deleteTrackByTrackNumberFromRecording(req: ServerRequest) =
            repository.findById(req.pathVariable("id"))
                    .doOnNext { it.tracks.dropWhile { track -> compareTrackToPathVariable(track, req) } }
                    .doOnNext { repository.save(it) }
                    .flatMap { noContent().build() }
}

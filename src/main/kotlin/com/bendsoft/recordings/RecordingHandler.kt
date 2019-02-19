package com.bendsoft.recordings

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.dao.DuplicateKeyException
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.*
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.bodyToServerSentEvents
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
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
                    .doOnNext { noContent().build() })

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
                    .doOnNext {
                        it.t1.tracks
                                .find { track -> compareTrackToPathVariable(track, req) }
                                ?.let { track -> throw DuplicateKeyException("Recording already has a track with number ${track.trackNumber}") }
                                .apply { it.t1.tracks.plus(it.t2) }
                    }
                    .map { it.t1 }
                    .doOnNext { repository.save(it) }
                    .doOnNext { ok().body(noContent().build()) }
                    .doOnError { unprocessableEntity().body(it.localizedMessage.toMono()) }

    fun deleteTrackByTrackNumberFromRecording(req: ServerRequest) =
            ok().body(repository.findById(req.pathVariable("id"))
                    .doOnNext { it.tracks.dropWhile { track -> compareTrackToPathVariable(track, req) } }
                    .doOnNext { repository.save(it) }
                    .doOnNext { noContent().build() })
}

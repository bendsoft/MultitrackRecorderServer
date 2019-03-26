package com.bendsoft.recordings

import com.bendsoft.shared.ErrorMessages
import com.bendsoft.shared.ServerResponseMessageFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.BodyInserters.fromObject
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
                    .switchIfEmpty(notFound().build())

    fun findOnDate(req: ServerRequest) =
            ok().body(repository.findAllRecordingOnDate(
                    LocalDate.parse(req.pathVariable("date"))
            ))
                    .switchIfEmpty(notFound().build())

    fun stream(req: ServerRequest) =
            ok().bodyToServerSentEvents(repository.findAll())

    fun create(req: ServerRequest) =
            req.bodyToMono(Recording::class.java)
                    .doOnNext { repository.save(it) }
                    .flatMap { created(URI.create("/recordings/$it")).build() }

    fun update(req: ServerRequest) =
            repository.findById(req.pathVariable("id"))
                    .zipWith(req.bodyToMono(Recording::class.java))
                    .map {
                        it.t1.copy(
                                name = it.t2.name,
                                recordingDate = it.t2.recordingDate,
                                tracks = it.t1.tracks.union(it.t2.tracks).toList()
                        )
                    }
                    .flatMap { ok().body(repository.save(it), Recording::class.java) }

    fun delete(req: ServerRequest) =
            repository.deleteById(req.pathVariable("id"))
                    .flatMap { noContent().build() }

    fun findAllTracksOfRecording(req: ServerRequest) =
            repository.findById(req.pathVariable("id"))
                    .flatMap { ok().body(fromObject(it.tracks)) }
                    .switchIfEmpty(notFound().build())

    fun findTrackByTrackNumberInRecording(req: ServerRequest) =
            repository.findById(req.pathVariable("id"))
                    .map { it.tracks.find { track -> track.trackNumber == req.pathVariable("trackNumber").toInt() } }
                    .flatMap { ok().body(Mono.justOrEmpty(it), Track::class.java) }
                    .switchIfEmpty(notFound().build())

    fun removeTrackByTrackNumberFromRecording(req: ServerRequest) =
            repository.findById(req.pathVariable("id"))
                    .flatMap {
                        val trackNumberToDelete = req.pathVariable("trackNumber").toInt()
                        val trackFound = it.tracks.find { track -> track.trackNumber == trackNumberToDelete }
                        if (trackFound == null)
                            ServerResponseMessageFactory.create(
                                    ErrorMessages.TRACK_NOT_FOUND,
                                    entity = it
                            )
                        else {
                            val updatedRecording = it.copy(
                                    tracks = it.tracks.minus(trackFound)
                            )
                            ok().body(repository.save(updatedRecording), Recording::class.java)
                        }
                    }
}

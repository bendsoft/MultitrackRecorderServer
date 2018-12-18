package com.bendsoft.recordings

import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate

@Repository
interface RecordingRepository : ReactiveMongoRepository<Recording, String> {
    @Query("{ 'recordingDate' : { \$gte: ?0, \$lte: ?0 } }")
    fun findAllRecordingOnDate(date: LocalDate): Flux<Recording>

    fun findAllTracks(recordingId: String, trackId: String): Mono<Track>

    fun findTrackById(recordingId: String): Flux<Track>

    fun saveTrack(recordingId: String, track: Track): Mono<Recording>

    fun deleteTrackById(recordingId: String, trackId: String): Mono<Void>
}
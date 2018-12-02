package com.bendsoft.tracks

import com.bendsoft.tracks.Track
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface TrackReactiveRepository : ReactiveMongoRepository<Track, String> {
}
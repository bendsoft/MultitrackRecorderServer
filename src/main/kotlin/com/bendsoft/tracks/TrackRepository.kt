package com.bendsoft.tracks

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface TrackRepository : ReactiveMongoRepository<Track, String> {
}
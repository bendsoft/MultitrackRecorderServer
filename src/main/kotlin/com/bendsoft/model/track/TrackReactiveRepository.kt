package com.bendsoft.model.track

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TrackReactiveRepository : ReactiveMongoRepository<Track, String> {
}
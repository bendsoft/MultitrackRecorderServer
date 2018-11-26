package com.bendsoft.model.track

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TrackBlockingRepository : CrudRepository<Track, String> {
}
package ch.ideadapt.springreactive.track

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TrackRepository : CrudRepository<Track, String> {
}
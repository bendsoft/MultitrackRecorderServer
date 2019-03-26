package com.bendsoft.recordings

import org.junit.Assert.assertEquals
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBodyList
import reactor.test.StepVerifier
import java.time.LocalDate


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class RecordingIntegrationTest {
    val COLLECTION_NAME = "recordings"

    @Autowired
    private lateinit var recordingRepository: RecordingRepository
    @Autowired
    private lateinit var operations: ReactiveMongoOperations
    @Autowired
    private lateinit var webClient: WebTestClient

    fun insert(recording: Recording) =
            recordingRepository.insert(recording)
                    .map { it.id }
                    .block()

    fun insertMany(recordings: List<Recording>) =
            recordingRepository.insert(recordings)
                    .map { it.id }
                    .collectList()
                    .block()!!

    fun createTracks(count: Int, startWith: Int? = 1): List<Track> =
            (1..count).map { Track(name = "Track #$it", trackNumber = it) }

    @BeforeEach
    fun initDB() {
        operations.createCollection(COLLECTION_NAME).block()
    }

    @AfterEach
    fun dropCollection() {
        operations.dropCollection(COLLECTION_NAME).block()
    }

    @Test
    fun `Find all tracks in a recording`() {
        val recordingId = insert(Recording(
                name = "Testrecording 1",
                recordingDate = LocalDate.now(),
                tracks = createTracks(2)
        ))

        webClient.get().uri("/api/recordings/$recordingId/tracks")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBodyList<Track>()
                .hasSize(2)
    }

    @Test
    fun `Find single track by trackNumber`() {
        val tracks = createTracks(5)
        val trackNumber = tracks[3].trackNumber

        val recordingIds = insertMany(listOf(
                Recording(
                        name = "Testrecording 1",
                        recordingDate = LocalDate.now(),
                        tracks = tracks
                ),
                Recording(
                        name = "Testrecording 2",
                        recordingDate = LocalDate.now(),
                        tracks = tracks
                )
        ))

        webClient.get().uri("/api/recordings/${recordingIds[0]}/tracks/$trackNumber")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody(Track::class.java)
                .returnResult().apply { assertEquals(4, responseBody?.trackNumber) }
    }

    @Test
    fun `Add Track to Recording`() {
        val tracks = createTracks(5)
        val recordingId = insert(
                Recording(
                        name = "Testrecording 2",
                        recordingDate = LocalDate.now(),
                        tracks = tracks.subList(0, 3)
                )
        )

        webClient.put().uri("/api/recordings/$recordingId/track")
                .accept(APPLICATION_JSON).contentType(APPLICATION_JSON)
                .syncBody(tracks.last())
                .exchange()
                .expectStatus().isOk
                .expectBody(Recording::class.java)
                .returnResult()
                .apply { assertEquals(4, responseBody?.tracks?.count()) }
                .apply {
                    StepVerifier
                            .create(recordingRepository
                                    .findById(recordingId.orEmpty())
                                    .map { it.tracks.last() }
                            )
                            .expectNext(tracks.last())
                            .verifyComplete()
                }
    }

    @Test
    fun `Add Track to Recording when trackNumber already occupied should return 4xx`() {
        val tracks = createTracks(5)
        val recordingIds = insertMany(listOf(
                Recording(
                        name = "Testrecording 1",
                        recordingDate = LocalDate.now(),
                        tracks = tracks.subList(0, 2)
                ),
                Recording(
                        name = "Testrecording 2",
                        recordingDate = LocalDate.now(),
                        tracks = tracks.subList(0, 3)
                )
        ))

        webClient.put().uri("/api/recordings/${recordingIds[0]}/track")
                .accept(APPLICATION_JSON).contentType(APPLICATION_JSON)
                .syncBody(tracks[0])
                .exchange()
                .expectStatus().is4xxClientError
                .expectBody()
                .returnResult()
    }

    @Test
    fun `Remove Track from Recording`() {
        val tracks = createTracks(5)
        val recordingId = insert(
                Recording(
                        name = "Testrecording 1",
                        recordingDate = LocalDate.now(),
                        tracks = tracks
                )
        )

        webClient.put().uri("/api/recordings/$recordingId/tracks/2")
                .exchange()
                .expectStatus().isOk
                .expectBody(Recording::class.java)
                .returnResult()
                .apply { assertEquals(4, responseBody?.tracks?.count()) }
                .apply {
                    StepVerifier
                            .create(recordingRepository
                                    .findById(recordingId.orEmpty())
                                    .map { it.tracks.count() }
                            )
                            .expectNext(4)
                            .verifyComplete()
                }
    }
}

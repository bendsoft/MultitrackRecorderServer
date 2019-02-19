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

    fun insertMany(recordings: List<Recording>): MutableList<String?> =
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
                .expectStatus().is2xxSuccessful
                .expectBodyList<Track>()
                .hasSize(2)
    }

    @Test
    fun `Find single track by id`() {
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
                .expectStatus().is2xxSuccessful
                .expectBody(Track::class.java)
                .returnResult().apply { assertEquals(4, responseBody?.trackNumber) }
    }
}

package com.bendsoft.track

import com.bendsoft.model.track.Track
import com.fasterxml.jackson.databind.ObjectMapper
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import de.flapdoodle.embed.mongo.config.IMongodConfig
import org.bson.Document
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.env.Environment
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBodyList


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = ["spring.data.mongodb.port="])
class TrackIntegrationTest {

	@Autowired
	private lateinit var webClient: WebTestClient
	@Autowired
	private lateinit var mongoConfig: IMongodConfig
	@Autowired
	private lateinit var env: Environment

	private lateinit var collection: MongoCollection<Document>
	private lateinit var dbOperations: MongoTemplate

	fun createDBAndCollection(collectionName: String) {
		val dbName = env.getProperty("spring.data.mongodb.database", "undefined")
		val dbHost = env.getProperty("spring.data.mongodb.host", "undefined")
		val client = MongoClient(dbHost, mongoConfig.net().port)
		val db = client.getDatabase(dbName)
		db.createCollection(collectionName)
		collection = db.getCollection(collectionName)
		dbOperations = MongoTemplate(client, dbName)
	}

	fun insert(obj: Any) {
		insertMany(listOf(obj))
	}

	fun insertMany(objs: Iterable<Any>) {
		val documents: Iterable<Document> = objs
				.map { ObjectMapper().writeValueAsString(it) }
				.map { Document.parse(it) }
		collection.insertMany(documents.toList())
	}

	@BeforeEach
	fun initDB(){
		createDBAndCollection("tracks")
	}

	@AfterEach
	fun dropCollection(){
		collection.drop()
	}

	@Test
	fun `Find all tracks on JSON endpoint`() {
		insert(Track(name = "Track #1"))
		insertMany(listOf(Track(name = "Track #2"), Track(name = "Track #3")))

		webClient.get().uri("/reactive/tracks")
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().is2xxSuccessful
				.expectBodyList<Track>()
				.hasSize(3)
	}

	@Test
	fun `Find single track by id`() {
		(1..3).map { Track(name = "Track #$it") }.apply { insertMany(this) }

		val id = dbOperations.findAll(Track::class.java).first().id

		webClient.get().uri("/reactive/tracks/$id")
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().is2xxSuccessful
				// see https://jira.spring.io/browse/SPR-15692
				.expectBody(Track::class.java).returnResult().apply { assertEquals("Track #1", responseBody?.name) }
	}
}
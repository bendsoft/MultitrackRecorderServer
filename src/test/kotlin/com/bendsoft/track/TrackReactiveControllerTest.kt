package com.bendsoft.track

import com.fasterxml.jackson.databind.ObjectMapper
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBodyList
import de.flapdoodle.embed.mongo.config.IMongodConfig
import org.bson.Document
import org.springframework.core.env.Environment


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = ["spring.data.mongodb.port="])
class IntegrationTests {

	@Autowired
	private lateinit var webClient: WebTestClient

	@Autowired
	private lateinit var mongoConfig: IMongodConfig
	private lateinit var collection: MongoCollection<Document>

	@Autowired
	private lateinit var env: Environment

	fun createDBAndCollection(collectionName: String){
		val dbName = env.getProperty("spring.data.mongodb.database", "undefined")
		val dbHost = env.getProperty("spring.data.mongodb.host", "undefined")
		val client = MongoClient(dbHost, mongoConfig.net().port)
		val db = client.getDatabase(dbName)
		db.createCollection(collectionName)
		collection = db.getCollection(collectionName)
	}

	fun insert(obj: Any){
		val document:String = ObjectMapper().writeValueAsString(obj)
		collection.insertOne(Document.parse(document))
	}

	@Test
	fun `Find all tracks on JSON REST endpoint`() {
		createDBAndCollection("track")

		insert(Track(id = "1", name = "Track #1"))
		insert(Track(id = "2", name = "Track #2"))

		webClient.get().uri("/reactive/tracks")
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().is2xxSuccessful
				.expectBodyList<Track>()
				.hasSize(2);
	}
}
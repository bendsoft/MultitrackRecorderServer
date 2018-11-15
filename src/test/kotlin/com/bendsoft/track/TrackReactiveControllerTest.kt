package com.bendsoft.track

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


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = ["spring.data.mongodb.port="])
class IntegrationTests {

	@Autowired
	private val webClient: WebTestClient? = null

	@Autowired
	private val mongoConfig: IMongodConfig? = null
	var collection: MongoCollection<Document>? = null

	fun createDBAndCollection(collectionName: String){
		val mongo = MongoClient("127.0.0.1", mongoConfig!!.net().port)
		val db = mongo.getDatabase("local")
		db.createCollection(collectionName)
		collection = db.getCollection(collectionName)
	}

	@Test
	fun `Find all tracks on JSON REST endpoint`() {
		createDBAndCollection("track")
		collection?.insertOne(Document("key", "val"))

		webClient!!.get().uri("/reactive/tracks")
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().is2xxSuccessful
				.expectBodyList<Track>()
				.hasSize(1);
	}
}
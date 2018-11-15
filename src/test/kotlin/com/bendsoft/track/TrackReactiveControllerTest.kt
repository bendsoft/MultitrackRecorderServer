package com.bendsoft.track

import com.mongodb.MongoClient
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
	private val client: WebTestClient? = null

	@Autowired
	private val mongoConfig: IMongodConfig? = null

	@Test
	fun `Find all tracks on JSON REST endpoint`() {
		val mongo = MongoClient("127.0.0.1", mongoConfig!!.net().port)
		val db = mongo.getDatabase("admin")
		db.createCollection("track")
		val col = db.getCollection("track")
		col.insertOne(Document("key", "val"))

		client!!.get().uri("/reactive/tracks")
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().is2xxSuccessful
				.expectBodyList<Track>()
				.hasSize(1);
	}
}
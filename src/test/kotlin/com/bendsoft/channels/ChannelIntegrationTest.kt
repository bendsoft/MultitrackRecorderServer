package com.bendsoft.channels

import com.fasterxml.jackson.databind.ObjectMapper
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import de.flapdoodle.embed.mongo.config.IMongodConfig
import org.bson.Document
import org.junit.Assert
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.env.Environment
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBodyList

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class ChannelIntegrationTests {
    @Autowired
    private lateinit var webClient: WebTestClient
    @Autowired
    private lateinit var mongoConfig: IMongodConfig
    @Autowired
    private lateinit var env: Environment

    private lateinit var collection: MongoCollection<Document>
    private lateinit var dbOperations: MongoTemplate

    fun createDBAndCollection(collectionName: String) {
        val dbName = env.getProperty("spring.data.mongodb.database", "undefined");
        val dbHost = env.getProperty("spring.data.mongodb.host", "undefined")
        val client = MongoClient(dbHost, mongoConfig.net().port)
        val db = client.getDatabase(dbName)

        db.createCollection(collectionName)
        collection = db.getCollection(collectionName)
        dbOperations = MongoTemplate(client, dbName)
    }

    fun insert(obj: Channel) {
        insertMany(listOf(obj))
    }

    fun insertMany(objs: Iterable<Channel>) {
        val documents: Iterable<Document> = objs
                .map { ObjectMapper().writeValueAsString(it) }
                .map { Document.parse(it) }
        collection.insertMany(documents.toList())
    }

    private val COLLECTION_NAME = "channels"

    @BeforeEach
    fun initDB() {
        createDBAndCollection(COLLECTION_NAME)
    }

    @AfterEach
    fun dropCollection() {
        collection.drop()
    }

    @Test
    fun `Find all channels on JSON endpoint`() {
        insert(
                Channel(id = "11", name = "Channel #1", channelNumber = 1, active = true)
        )

        insertMany(
                listOf(
                        Channel(id = "22", name = "Channel #2", channelNumber = 2, active = true),
                        Channel(id = "33", name = "Channel #3", channelNumber = 3, active = true)
                )
        )

        webClient.get().uri("/api/channels")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is2xxSuccessful
                .expectBodyList<Channel>()
                .hasSize(3)
    }

    @Test
    fun `Find single channel by id`() {
        (1..3)
                .map { Channel(name = "Channel #$it", channelNumber = it, active = true) }
                .apply { insertMany(this) }

        val id = dbOperations.findAll(Channel::class.java).first().id

        webClient.get().uri("/api/channels/$id")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is2xxSuccessful
                // see https://jira.spring.io/browse/SPR-15692
                .expectBody(Channel::class.java)
                .returnResult().apply { Assert.assertEquals("Channel #1", responseBody?.name) }
    }
}

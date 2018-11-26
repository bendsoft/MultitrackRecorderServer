package com.bendsoft

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@SpringBootApplication
@EnableMongoRepositories
class MultitrackRecorderServerApplication

fun main(args: Array<String>) {
	runApplication<MultitrackRecorderServerApplication>(*args)
}

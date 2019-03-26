package com.bendsoft

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableMongoRepositories
@EnableAsync
class MultitrackRecorderServerApplication

fun main(args: Array<String>) {
	runApplication<MultitrackRecorderServerApplication>(*args)
}

package com.bendsoft.multitrackrecorderserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MultitrackRecorderServerApplication

fun main(args: Array<String>) {
    runApplication<MultitrackRecorderServerApplication>(*args)
}

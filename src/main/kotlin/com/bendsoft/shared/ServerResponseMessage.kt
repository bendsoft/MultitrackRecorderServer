package com.bendsoft.shared

import org.springframework.http.HttpStatus
import org.springframework.http.ReactiveHttpOutputMessage
import org.springframework.web.reactive.function.BodyInserter
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerResponse

enum class LEVEL {
    ERROR, INFO, WARNING
}

interface ServerResponseMessage {
    val level: LEVEL
    val message: String
    val code: Int
    val entity: Any?
}

class ServerResponseMessageFactory {
    companion object {
        fun create(
                level: LEVEL,
                message: String,
                code: Int,
                entity: Any?
        ): BodyInserter<ServerResponseMessage, ReactiveHttpOutputMessage> =
                BodyInserters.fromObject(
                        object : ServerResponseMessage {
                            override val level = level
                            override val message = message
                            override val code = code
                            override val entity = entity
                        }
                )

        fun create(
                status: HttpStatus,
                level: LEVEL,
                message: String,
                code: Int,
                entity: Any?
        ) = create(status).body(create(level, message, code, entity))

        fun create(status: HttpStatus) = ServerResponse.status(status)
    }
}

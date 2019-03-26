package com.bendsoft.shared

import org.springframework.http.HttpStatus
import org.springframework.http.ReactiveHttpOutputMessage
import org.springframework.web.reactive.function.BodyInserter
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerResponse

object ServerResponseMessageFactory {
    fun create(
            level: MessageLevel,
            message: String,
            code: Int = 0,
            entity: Any? = null
    ): BodyInserter<ResponseMessage, ReactiveHttpOutputMessage> =
            BodyInserters.fromObject(
                    object : ResponseMessage {
                        override val level = level
                        override val message = message
                        override val code = code
                        override val entity = entity
                    }
            )

    fun create(
            status: HttpStatus,
            level: MessageLevel,
            message: String,
            code: Int = 0,
            entity: Any? = null
    ) = create(status).body(create(level, message, code, entity))

    fun create(
            responseMessage: com.bendsoft.shared.MTRServerResponse,
            entity: Any? = null
    )= create(responseMessage.status).body(
            create(
                    responseMessage.message.level,
                    responseMessage.message.message,
                    responseMessage.message.code,
                    entity
            )
    )

    fun create(status: HttpStatus) = ServerResponse.status(status)
}
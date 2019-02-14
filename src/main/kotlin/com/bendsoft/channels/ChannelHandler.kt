package com.bendsoft.channels

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.bodyToServerSentEvents

@Configuration
class ChannelHandler {
    @Autowired
    lateinit var repository: ChannelRepository

    fun findAll(req: ServerRequest) =
            ok().body(repository.findAll())

    fun findById(req: ServerRequest) =
            ok().body(repository.findById(req.pathVariable("id")))

    fun stream(req: ServerRequest) =
            ok().bodyToServerSentEvents(repository.findAll())
}

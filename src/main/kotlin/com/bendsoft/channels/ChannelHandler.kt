package com.bendsoft.channels

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.*
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.bodyToServerSentEvents
import reactor.core.publisher.Mono
import java.net.URI

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

    fun create(req: ServerRequest): Mono<ServerResponse> =
        req.bodyToMono(Channel::class.java)
                .flatMap { repository.save(it) }
                .flatMap { created(URI.create("/channels/$it")).build() }

    fun update(req: ServerRequest): Mono<ServerResponse> =
            repository.findById(req.pathVariable("id"))
                    .zipWith(req.bodyToMono(Channel::class.java))
                    .map { it.t1.copy(
                            name = it.t2.name,
                            channelNumber = it.t2.channelNumber,
                            active = it.t2.active
                    ) }
                    .flatMap { repository.save(it) }
                    .flatMap { noContent().build() }

    fun delete(req: ServerRequest): Mono<ServerResponse> =
        repository.deleteById(req.pathVariable("id"))
                .flatMap { noContent().build() }
}

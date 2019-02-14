package com.bendsoft.channels

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ChannelRepository : ReactiveMongoRepository<Channel, String>

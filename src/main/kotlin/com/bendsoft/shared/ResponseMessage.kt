package com.bendsoft.shared

interface ResponseMessage {
    val level: MessageLevel
    val message: String
    val code: Int
    val entity: Any?
}
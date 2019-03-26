package com.bendsoft.shared

import org.springframework.http.HttpStatus

interface MTRServerResponse {
    val status: HttpStatus
    val message: ResponseMessage
}
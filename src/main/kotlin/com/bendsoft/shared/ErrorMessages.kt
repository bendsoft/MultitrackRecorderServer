package com.bendsoft.shared

import org.springframework.http.HttpStatus

enum class ErrorMessages : MTRServerResponse {
    TRACK_NOT_FOUND {
        override val status = HttpStatus.BAD_REQUEST
        override val message = object : ResponseMessage {
            override val level = MessageLevel.ERROR
            override val code = -1
            override val message = "Track could not be found in the given recording"
            override val entity: Nothing? = null
        }
    },
    RECORDING_IN_PROGRESS {
        override val status = HttpStatus.BAD_REQUEST
        override val message = object : ResponseMessage {
            override val level = MessageLevel.ERROR
            override val code = -2
            override val message = "Recording in progress"
            override val entity: Nothing? = null
        }
    },
    NO_RECORDING_IN_PROGRESS {
        override val status = HttpStatus.BAD_REQUEST
        override val message = object : ResponseMessage {
            override val level = MessageLevel.ERROR
            override val code = -3
            override val message = "Cannot stop recording, no Recording is currently running."
            override val entity: Nothing? = null
        }
    },
    DUPLICATE_TRACK {
        override val status = HttpStatus.BAD_REQUEST
        override val message = object : ResponseMessage {
            override val level = MessageLevel.ERROR
            override val code = -4
            override val message = "Track already exists"
            override val entity: Nothing? = null
        }
    }
}
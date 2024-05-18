package com.jet.gameservice.dto

import com.jet.gameservice.exception.GameException

class ErrorMessageResponseDto {
    var errorMessage: String?
        private set
    var errorCode = "UNKNOWN"
        private set

    constructor(errorMessage: String?, errorCode: String) : super() {
        this.errorMessage = errorMessage
        this.errorCode = errorCode
    }

    constructor(t: Throwable) {
        errorMessage = t.message
        if (t is GameException) {
            val ge: GameException = t
            if (ge.errorCode != null) {
                errorCode = ge.errorCode!!
            }
        }
    }

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("ErrorMessageResponseDto [errorMessage=")
        builder.append(errorMessage)
        builder.append(", errorCode=")
        builder.append(errorCode)
        builder.append("]")
        return builder.toString()
    }
}
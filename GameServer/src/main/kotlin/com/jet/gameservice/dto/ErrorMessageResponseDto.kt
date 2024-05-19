package com.jet.gameservice.dto

import com.jet.gameservice.exception.GameException

data class ErrorMessageResponseDto(
    val errorMessage: String?,
    val errorCode: String = "UNKNOWN"
) {
    constructor(t: Throwable) : this(
        errorMessage = t.message,
        errorCode = if (t is GameException && t.errorCode != null) t.errorCode!! else "UNKNOWN"
    )

    override fun toString(): String {
        return "ErrorMessageResponseDto(errorMessage=$errorMessage, errorCode=$errorCode)"
    }
}
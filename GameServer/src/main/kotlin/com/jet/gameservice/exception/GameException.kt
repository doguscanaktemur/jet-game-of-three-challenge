package com.jet.gameservice.exception

import com.jet.gameservice.enums.Error

class GameException : RuntimeException {

    var errorCode: String? = null
        private set
    
    constructor() : super()

    constructor(message: String) : super(message)

    constructor(error: Error) : super(error.message) {
        errorCode = error.code
    }

    constructor(error: Error, vararg args: Any) : super(String.format(error.message, *args)) {
        errorCode = error.code
    }

    constructor(cause: Throwable) : super(cause)

    constructor(message: String, cause: Throwable) : super(message, cause)

    constructor(error: Error, cause: Throwable, vararg args: Any) : super(String.format(error.message, *args), cause) {
        errorCode = error.code
    }

    constructor(message: String, cause: Throwable, enableSuppression: Boolean, writableStackTrace: Boolean) : super(message, cause, enableSuppression, writableStackTrace)
    
}
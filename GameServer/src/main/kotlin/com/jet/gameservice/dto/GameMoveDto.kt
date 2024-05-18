package com.jet.gameservice.dto

data class GameMoveDto( var resultingNumber: Int? = null, var added: Int? = null) {

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("GameMoveDto [resultingNumber=")
        builder.append(resultingNumber)
        builder.append(", added=")
        builder.append(added)
        builder.append("]")
        return builder.toString()
    }
}
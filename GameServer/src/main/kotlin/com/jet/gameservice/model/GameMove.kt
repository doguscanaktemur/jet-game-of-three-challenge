package com.jet.gameservice.model

import com.jet.gameservice.dto.GameMoveDto
import com.jet.gameservice.enums.Error
import com.jet.gameservice.exception.GameException
import java.util.stream.Collectors
import java.util.stream.Stream

class GameMove(var resultingNumber: Int? = null, var added: Int? = null) {

    constructor(gameMoveDto: GameMoveDto) : this() {
        resultingNumber = gameMoveDto.resultingNumber
        if (resultingNumber != null && resultingNumber!! < 2) {
            throw GameException(Error.RESULTING_NUMBER_TOO_SMALL, resultingNumber!!)
        }
        added = gameMoveDto.added
        if (added != null && !VALID_ADDED_VALUES.contains(added)) {
            throw GameException(Error.ILLEGAL_ADDED_NUMBER, added!!)
        }
    }

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("GameMove [resultingNumber=")
        builder.append(resultingNumber)
        builder.append(", added=")
        builder.append(added)
        builder.append("]")
        return builder.toString()
    }

    companion object {
        private val VALID_ADDED_VALUES = Stream.of(1, 0, -1).collect(
            Collectors.toCollection { HashSet() }
        )
    }
}
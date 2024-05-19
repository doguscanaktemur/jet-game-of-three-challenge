package com.jet.gameservice.unit.model

import com.jet.gameservice.dto.GameMoveDto
import com.jet.gameservice.enums.Error
import com.jet.gameservice.exception.GameException
import com.jet.gameservice.model.GameMove
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GameMoveTest {

    @Test
    fun `constructor should initialize correctly with valid GameMoveDto`() {
        val gameMoveDto = GameMoveDto(resultingNumber = 5, added = 0)

        val gameMove = GameMove(gameMoveDto)

        assertEquals(5, gameMove.resultingNumber)
        assertEquals(0, gameMove.added)
    }

    @Test
    fun `constructor should throw GameException for invalid resulting number`() {
        val gameMoveDto = GameMoveDto(resultingNumber = 1, added = 0)

        val exception = assertThrows<GameException> { GameMove(gameMoveDto) }
        assertEquals(Error.RESULTING_NUMBER_TOO_SMALL.code, exception.errorCode)
    }

    @Test
    fun `constructor should throw GameException for illegal added number`() {
        val gameMoveDto = GameMoveDto(resultingNumber = 5, added = 2)

        val exception = assertThrows<GameException> { GameMove(gameMoveDto) }
        assertEquals(Error.ILLEGAL_ADDED_NUMBER.code, exception.errorCode)
    }
}

package com.jet.gameservice.unit.domain

import com.jet.gameservice.domain.GameImpl
import com.jet.gameservice.dto.GameMoveDto
import com.jet.gameservice.enums.Error
import com.jet.gameservice.enums.Notification
import com.jet.gameservice.exception.GameException
import com.jet.gameservice.unit.helper.MockitoHelper
import com.jet.gameservice.model.GameMove
import com.jet.gameservice.service.WebSocketMessageSendingService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*

class GameImplTest {

    private lateinit var game: GameImpl
    private lateinit var webSocketMessageSendingService: WebSocketMessageSendingService

    @BeforeEach
    fun setUp() {
        webSocketMessageSendingService = mock(WebSocketMessageSendingService::class.java)
        game = GameImpl(webSocketMessageSendingService)
    }

    @Test
    fun `addUserToGame should add users correctly`() {
        game.addUserToGame("user1")
        game.addUserToGame("user2")

        assertTrue(game.isUserInGame("user1"))
        assertTrue(game.isUserInGame("user2"))
    }

    @Test
    fun `addUserToGame should not add more than two users`() {
        game.addUserToGame("user1")
        game.addUserToGame("user2")
        game.addUserToGame("user3")

        assertFalse(game.isUserInGame("user3"))
    }

    @Test
    fun `playGameMoveForUser should throw error if it's not user's turn`() {
        game.addUserToGame("user1")
        game.addUserToGame("user2")

        val gameMoveDto = GameMoveDto(resultingNumber = 6, added = null)

        assertThrows<GameException> { game.playGameMoveForUser("user2", gameMoveDto) }

        val exception = assertThrows<GameException> { game.playGameMoveForUser("user2", gameMoveDto) }
        assertEquals(Error.NOT_YOUR_TURN.code, exception.errorCode)
    }

    @Test
    fun `playGameMoveForUser should throw error if resultingNumber is null on first move`() {
        game.addUserToGame("user1")
        game.addUserToGame("user2")

        val gameMoveDto = GameMoveDto(resultingNumber = null, added = null)

        val exception = assertThrows<GameException> { game.playGameMoveForUser("user1", gameMoveDto) }
        assertEquals(Error.PARAMETER_IS_NULL.code, exception.errorCode)
    }

    @Test
    fun `playGameMoveForUser should throw error if added is null on subsequent move`() {
        game.addUserToGame("user1")
        game.addUserToGame("user2")

        val gameMoveDto1 = GameMoveDto(resultingNumber = 6, added = null)
        game.playGameMoveForUser("user1", gameMoveDto1)

        val gameMoveDto2 = GameMoveDto(resultingNumber = null, added = null)

        val exception = assertThrows<GameException> { game.playGameMoveForUser("user2", gameMoveDto2) }
        assertEquals(Error.PARAMETER_IS_NULL.code, exception.errorCode)
    }

    @Test
    fun `playGameMoveForUser should throw error if added value makes resultingNumber not divisible by 3`() {
        game.addUserToGame("user1")
        game.addUserToGame("user2")

        val gameMoveDto1 = GameMoveDto(resultingNumber = 6, added = null)
        game.playGameMoveForUser("user1", gameMoveDto1)

        val gameMoveDto2 = GameMoveDto(resultingNumber = null, added = 1)

        val exception = assertThrows<GameException> { game.playGameMoveForUser("user2", gameMoveDto2) }
        assertEquals(Error.NOT_DIVISIBLE_BY_3.code, exception.errorCode)
    }

    @Test
    fun `playGameMoveForUser should process first move correctly`() {
        game.addUserToGame("user1")
        game.addUserToGame("user2")

        val gameMoveDto = GameMoveDto(resultingNumber = 6, added = null)
        game.playGameMoveForUser("user1", gameMoveDto)

        // Capturing arguments
        val argumentCaptorUser: ArgumentCaptor<String> = ArgumentCaptor.forClass(String::class.java)
        val argumentCaptorGameMove: ArgumentCaptor<GameMove> = ArgumentCaptor.forClass(GameMove::class.java)

        // Verify that the game move was sent to both users in the correct order
        verify(webSocketMessageSendingService, times(2))
            .sendGameMoveToUser(MockitoHelper.capture(argumentCaptorUser), MockitoHelper.capture(argumentCaptorGameMove))

        // Verify the captured arguments
        val capturedUsers: List<String> = argumentCaptorUser.allValues
        val capturedGameMoves: List<GameMove> = argumentCaptorGameMove.allValues

        assertEquals("user2", capturedUsers[0])
        assertEquals(6, capturedGameMoves[0].resultingNumber)
        assertNull(capturedGameMoves[0].added)

        assertEquals("user1", capturedUsers[1])
        assertEquals(6, capturedGameMoves[1].resultingNumber)
        assertNull(capturedGameMoves[1].added)
    }

    @Test
    fun `playGameMoveForUser should process subsequent moves correctly`() {
        game.addUserToGame("user1")
        game.addUserToGame("user2")

        val gameMoveDto1 = GameMoveDto(resultingNumber = 6, added = null)
        game.playGameMoveForUser("user1", gameMoveDto1)

        val gameMoveDto2 = GameMoveDto(resultingNumber = null, added = 0)
        game.playGameMoveForUser("user2", gameMoveDto2)

        // Capturing arguments
        val argumentCaptorUser: ArgumentCaptor<String> = ArgumentCaptor.forClass(String::class.java)
        val argumentCaptorGameMove: ArgumentCaptor<GameMove> = ArgumentCaptor.forClass(GameMove::class.java)

        // Verify that the game move was sent to both users in the correct order
        verify(webSocketMessageSendingService, times(4))
            .sendGameMoveToUser(MockitoHelper.capture(argumentCaptorUser), MockitoHelper.capture(argumentCaptorGameMove))

        // Verify the captured arguments
        val capturedUsers: List<String> = argumentCaptorUser.allValues
        val capturedGameMoves: List<GameMove> = argumentCaptorGameMove.allValues

        assertEquals("user2", capturedUsers[0])
        assertEquals(6, capturedGameMoves[0].resultingNumber)
        assertNull(capturedGameMoves[0].added)

        assertEquals("user1", capturedUsers[1])
        assertEquals(6, capturedGameMoves[1].resultingNumber)
        assertNull(capturedGameMoves[1].added)

        assertEquals("user1", capturedUsers[2])
        assertEquals(0, capturedGameMoves[2].added)
        assertEquals(6, capturedGameMoves[2].resultingNumber)

        assertEquals("user2", capturedUsers[3])
        assertEquals(0, capturedGameMoves[2].added)
        assertEquals(6, capturedGameMoves[2].resultingNumber)

    }

    @Test
    fun `playGameMoveForUser should notify winner correctly`() {
        game.addUserToGame("user1")
        game.addUserToGame("user2")

        val gameMoveDto1 = GameMoveDto(resultingNumber = 6, added = null)
        game.playGameMoveForUser("user1", gameMoveDto1)

        val gameMoveDto2 = GameMoveDto(resultingNumber = null, added = 0)
        game.playGameMoveForUser("user2", gameMoveDto2)

        val gameMoveDto3 = GameMoveDto(resultingNumber = null, added = 1)
        game.playGameMoveForUser("user1", gameMoveDto3)

        verify(webSocketMessageSendingService).sendNotificationToUser("user1", Notification.YOU_WON)
        verify(webSocketMessageSendingService).sendNotificationToUser("user2", Notification.YOU_LOST)
    }

    @Test
    fun `removeUserFromGame should notify other user and reset game`() {
        game.addUserToGame("user1")
        game.addUserToGame("user2")

        game.removeUserFromGame("user1")

        verify(webSocketMessageSendingService).sendNotificationToUser("user2", Notification.OTHER_USER_DISCONNECTED)
        assertFalse(game.isUserInGame("user1"))
        assertFalse(game.isUserInGame("user2"))
    }

    @Test
    fun `isUserInGame should return correct status`() {
        game.addUserToGame("user1")

        assertTrue(game.isUserInGame("user1"))
        assertFalse(game.isUserInGame("user2"))
    }

    @Test
    fun `isFirstToPlay should return correct status`() {
        game.addUserToGame("user1")

        assertTrue(game.isFirstToPlay("user1"))
        assertFalse(game.isFirstToPlay("user2"))
    }

    @Test
    fun `getFirstResultingNumber should send game move if moves exist`() {
        game.addUserToGame("user1")

        val gameMoveDto = GameMoveDto(resultingNumber = 6, added = null)
        game.playGameMoveForUser("user1", gameMoveDto)

        game.addUserToGame("user2")
        game.getFirstResultingNumber("user2")

        // Capturing arguments
        val argumentCaptorUser: ArgumentCaptor<String> = ArgumentCaptor.forClass(String::class.java)
        val argumentCaptorGameMove: ArgumentCaptor<GameMove> = ArgumentCaptor.forClass(GameMove::class.java)

        // Verify that the game move was sent to both users in the correct order
        verify(webSocketMessageSendingService, times(2))
            .sendGameMoveToUser(MockitoHelper.capture(argumentCaptorUser), MockitoHelper.capture(argumentCaptorGameMove))

        // Verify the captured arguments
        val capturedUsers: List<String> = argumentCaptorUser.allValues
        val capturedGameMoves: List<GameMove> = argumentCaptorGameMove.allValues

        assertEquals("user1", capturedUsers[0])
        assertEquals(6, capturedGameMoves[0].resultingNumber)
        assertNull(capturedGameMoves[0].added)

        assertEquals("user2", capturedUsers[1])
        assertEquals(6, capturedGameMoves[1].resultingNumber)
        assertNull(capturedGameMoves[1].added)
    }

    @Test
    fun `isUserInGame should throw error if user is null`() {
        val exception = assertThrows<GameException> { game.isUserInGame(null) }
        assertEquals(Error.PARAMETER_IS_NULL.code, exception.errorCode)
    }

    @Test
    fun `isFirstToPlay should throw error if user is null`() {
        val exception = assertThrows<GameException> { game.isFirstToPlay(null) }
        assertEquals(Error.PARAMETER_IS_NULL.code, exception.errorCode)
    }

    @Test
    fun `getFirstResultingNumber should throw error if user is null`() {
        val exception = assertThrows<GameException> { game.getFirstResultingNumber(null) }
        assertEquals(Error.PARAMETER_IS_NULL.code, exception.errorCode)
    }

    @Test
    fun `getFirstResultingNumber should notify user to wait for another player if only one user in game`() {
        game.addUserToGame("user1")
        game.getFirstResultingNumber("user1")

        verify(webSocketMessageSendingService).sendNotificationToUser("user1", Notification.WAIT_OTHER_USER_JOIN_GAME)
    }
}

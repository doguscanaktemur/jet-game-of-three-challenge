package com.jet.gameclient.unit.service

import com.jet.gameclient.dto.FirstUserToPlayResponseDto
import com.jet.gameclient.dto.GameMoveDto
import com.jet.gameclient.dto.NotificationResponseDto
import com.jet.gameclient.service.GameServerClient
import com.jet.gameclient.service.GameService
import com.jet.gameclient.service.WebSocketService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.springframework.messaging.simp.stomp.StompSession


@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
internal class GameServiceTest {

    @Mock
    private lateinit var mockSession: StompSession

    @Mock
    private lateinit var gameServerClient: GameServerClient

    @Mock
    private lateinit var webSocketService: WebSocketService

    @InjectMocks
    private lateinit var gameService: GameService

    @BeforeEach
    fun setUp() {
        `when`(webSocketService.getSession()).thenReturn(mockSession)
    }

    @Test
    fun `test startAutomaticGame triggers automatic game logic`() {
        `when`(gameServerClient.isFirstToPlay(anyString())).thenReturn(FirstUserToPlayResponseDto(isFirstToPlay = true))

        gameService.startAutomaticGame()
        simulateConnection("testUser")

        verify(webSocketService).connect()
        verify(gameServerClient).isFirstToPlay("testUser")
        verify(mockSession).send(anyString(), any(GameMoveDto::class.java))
    }

    @Test
    fun `test startManualGame does not trigger automatic game logic`() {
        gameService.startManualGame()
        simulateConnection("testUser")

        verify(webSocketService).connect()
        verify(gameServerClient, never()).isFirstToPlay(anyString())
        verify(mockSession, never()).send(anyString(), any(GameMoveDto::class.java))
    }

    @Test
    fun `test handleNotifications for game end`() {
        val notification = NotificationResponseDto(code = "YOU_WON", text = "You have won the game")

        callPrivateHandleNotifications(notification)

        verify(webSocketService).disconnect()
    }

    @Test
    fun `test handleMoves processes game moves correctly`() {
        `when`(gameServerClient.isFirstToPlay(anyString())).thenReturn(FirstUserToPlayResponseDto(isFirstToPlay = true))

        gameService.startAutomaticGame()
        simulateConnection("testUser")

        val move = GameMoveDto(resultingNumber = 2)
        callPrivateHandleMoves(move)

        verify(mockSession).send(anyString(), any(GameMoveDto::class.java))
    }

    // Helper method to simulate the WebSocket connection
    private fun simulateConnection(userName: String) {
        val onConnectedMethod = GameService::class.java.getDeclaredMethod("onConnected", String::class.java)
        onConnectedMethod.isAccessible = true
        onConnectedMethod.invoke(gameService, userName)
    }

    // Helper method to call private handleMoves method
    private fun callPrivateHandleMoves(move: GameMoveDto) {
        val handleMovesMethod = GameService::class.java.getDeclaredMethod("handleMoves", GameMoveDto::class.java)
        handleMovesMethod.isAccessible = true
        handleMovesMethod.invoke(gameService, move)
    }

    // Helper method to call private handleNotifications method
    private fun callPrivateHandleNotifications(notification: NotificationResponseDto) {
        val handleNotificationsMethod =
            GameService::class.java.getDeclaredMethod("handleNotifications", NotificationResponseDto::class.java)
        handleNotificationsMethod.isAccessible = true
        handleNotificationsMethod.invoke(gameService, notification)
    }

}

package com.jet.gameclient.unit.service

import com.jet.gameclient.dto.GameMoveDto
import com.jet.gameclient.service.GameServerClient
import com.jet.gameclient.service.GameService
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.messaging.simp.stomp.StompSession


@ExtendWith(MockitoExtension::class)
internal class GameServiceTest {

    @Mock
    private lateinit var gameServerClient: GameServerClient

    @Spy
    @InjectMocks
    private var gameService: GameService = GameService("test")



    @Test
    fun testGenerateAutomaticNumber() {
        val stompSession = mock(StompSession::class.java)
        doReturn(stompSession).`when`(gameService).getSession()

        gameService.generateAutomaticNumber(null)

        verify(stompSession).send(
            eq("/app/send"),
            any(GameMoveDto::class.java)
        )
    }

    @Test
    fun testStartAutomaticGame(){
        doNothing().`when`(gameService).connect()

        gameService.startAutomaticGame()

        assertFalse(gameService.isManualPlay)
    }

    @Test
    fun testStartManualGame(){
        doNothing().`when`(gameService).connect()

        gameService.startManualGame()

        assertTrue(gameService.isManualPlay)
    }

    @Test
    fun testHandleMoves_AutomaticPlay() {
        val stompSession = mock(StompSession::class.java)
        doReturn(stompSession).`when`(gameService).getSession()

        val move = GameMoveDto(resultingNumber = 5, added = 2)
        gameService.isManualPlay = false
        gameService.numOfGameMove = 1
        gameService.isFirstToPlay = true

        gameService.handleMoves(move)

        // Verify behavior in automatic play mode
        verify(stompSession).send(
            eq("/app/send"),
            any(GameMoveDto::class.java)
        )
    }

    @Test
    fun testHandleMoves_ManualPlay() {
        val move = GameMoveDto(resultingNumber = 5, added = 2)
        gameService.isManualPlay = true
        gameService.numOfGameMove = 2
        gameService.isFirstToPlay = false

        gameService.handleMoves(move)

        // Verify behavior in manual play mode
        verify(gameService, never()).generateAutomaticNumber(any())
    }

}

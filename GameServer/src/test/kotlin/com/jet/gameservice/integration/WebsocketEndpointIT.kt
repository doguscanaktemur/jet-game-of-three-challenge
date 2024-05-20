package com.jet.gameservice.integration

import com.jet.gameservice.dto.GameMoveDto
import com.jet.gameservice.dto.NotificationResponseDto
import com.jet.gameservice.enums.Notification
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.simp.stomp.*
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import java.lang.reflect.Type
import java.util.concurrent.CompletableFuture
import kotlin.time.DurationUnit
import kotlin.time.toTimeUnit


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebsocketEndpointIT {

    @LocalServerPort
    private val port: Int? = null
    private lateinit var url: String

    companion object {
        val SEND_AFTER_CONNECT_ENDPOINT = "/app/after_connect"
        val SEND_GAME_MOVE_ENDPOINT = "/app/send"
        val SUBSCRIBE_NOTIFICATIONS_ENDPOINT = "/user/queue/notifications"
        val SUBSCRIBE_GAME_MOVES_ENDPOINT = "/user/queue/game_moves"
    }

    lateinit var notificationCompletableFuture: CompletableFuture<NotificationResponseDto>
    lateinit var gameMoveCompletableFuture: CompletableFuture<GameMoveDto>

    @BeforeEach
    fun setup() {
        notificationCompletableFuture = CompletableFuture<NotificationResponseDto>()
        gameMoveCompletableFuture = CompletableFuture<GameMoveDto>()
        url = "ws://localhost:$port/websocket"
    }


    @Test
    fun testWebsocketEndpoints() {
        val stompClient = WebSocketStompClient(StandardWebSocketClient())
        stompClient.messageConverter = MappingJackson2MessageConverter()

        val stompSession = stompClient.connectAsync(url!!, object : StompSessionHandlerAdapter() {})
            .get(1, DurationUnit.SECONDS.toTimeUnit())
        stompSession.subscribe(SUBSCRIBE_NOTIFICATIONS_ENDPOINT, object : StompFrameHandler {
            override fun getPayloadType(headers: StompHeaders): Type {
                return NotificationResponseDto::class.java
            }

            override fun handleFrame(headers: StompHeaders, payload: Any?) {
                notificationCompletableFuture.complete(payload as NotificationResponseDto)
            }
        })
        stompSession.subscribe(SUBSCRIBE_GAME_MOVES_ENDPOINT, object : StompFrameHandler {
            override fun getPayloadType(headers: StompHeaders): Type {
                return GameMoveDto::class.java
            }

            override fun handleFrame(headers: StompHeaders, payload: Any?) {
                gameMoveCompletableFuture.complete(payload as GameMoveDto)
            }
        })
        stompSession.send(SEND_AFTER_CONNECT_ENDPOINT, "test")
        val notification: NotificationResponseDto = notificationCompletableFuture.get(10, DurationUnit.SECONDS.toTimeUnit())
        assertNotNull(notification)
        assertEquals(Notification.WAIT_OTHER_USER_JOIN_GAME.code, notification.code)

        stompSession.send(SEND_GAME_MOVE_ENDPOINT, GameMoveDto(resultingNumber = 10))
        val gameMoveDto: GameMoveDto = gameMoveCompletableFuture.get(10, DurationUnit.SECONDS.toTimeUnit())
        assertNotNull(gameMoveDto)
        assertEquals(10, gameMoveDto.resultingNumber)
    }

    @Test
    fun testGameIsBusyWithTwoPlayers() {
        val stompClient1 = WebSocketStompClient(StandardWebSocketClient())
        stompClient1.messageConverter = MappingJackson2MessageConverter()

        stompClient1.connectAsync(url, object : StompSessionHandlerAdapter() {})
            .get(1, DurationUnit.SECONDS.toTimeUnit())

        val stompClient2 = WebSocketStompClient(StandardWebSocketClient())
        stompClient2.messageConverter = MappingJackson2MessageConverter()

        stompClient1.connectAsync(url, object : StompSessionHandlerAdapter() {})
            .get(1, DurationUnit.SECONDS.toTimeUnit())

        val stompClient3 = WebSocketStompClient(StandardWebSocketClient())
        stompClient3.messageConverter = MappingJackson2MessageConverter()

        val stompSession3 = stompClient1.connectAsync(url!!, object : StompSessionHandlerAdapter() {})
            .get(1, DurationUnit.SECONDS.toTimeUnit())

        stompSession3.subscribe(SUBSCRIBE_NOTIFICATIONS_ENDPOINT, object : StompFrameHandler {
            override fun getPayloadType(headers: StompHeaders): Type {
                return NotificationResponseDto::class.java
            }

            override fun handleFrame(headers: StompHeaders, payload: Any?) {
                notificationCompletableFuture.complete(payload as NotificationResponseDto)
            }
        })

        stompSession3.send(SEND_AFTER_CONNECT_ENDPOINT, "test")
        val notification: NotificationResponseDto = notificationCompletableFuture.get(10, DurationUnit.SECONDS.toTimeUnit())
        assertNotNull(notification)
        assertEquals(Notification.GAME_IS_BUSY_WITH_TWO_PLAYERS.code, notification.code)
    }

}
package com.jet.gameclient.service

import com.jet.gameclient.dto.ErrorMessageResponseDto
import com.jet.gameclient.dto.GameMoveDto
import com.jet.gameclient.dto.NotificationResponseDto
import jakarta.annotation.PreDestroy
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.simp.stomp.*
import org.springframework.stereotype.Service
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import java.lang.reflect.Type
import java.util.concurrent.CompletableFuture

@Service
@Profile("!test")
class WebSocketService(@Value("\${websocket.connection.url}") private val url: String) {
    private var connectedFuture = CompletableFuture<StompSession>()

    lateinit var moveHandler: ((GameMoveDto) -> Unit)
    lateinit var notificationHandler: ((NotificationResponseDto) -> Unit)
    lateinit var errorHandler: ((ErrorMessageResponseDto) -> Unit)
    lateinit var connectionHandler: ((String) -> Unit)


    fun connect() {
        val stompClient = createStompClient()

        connectedFuture = CompletableFuture()
        val future = stompClient.connectAsync(url, GameSessionHandler())
        handleConnectionResult(future)
    }

    fun createStompClient(): WebSocketStompClient {
        return WebSocketStompClient(StandardWebSocketClient()).apply {
            messageConverter = MappingJackson2MessageConverter()
        }
    }

    private fun handleConnectionResult(future: CompletableFuture<StompSession>) {
        future.whenComplete { _, ex ->
            if (ex == null) {
                println("Connection established successfully")
            } else {
                println("Failed to connect: ${ex.message}")
            }
        }
    }

    private inner class GameSessionHandler : StompSessionHandlerAdapter() {
        override fun afterConnected(session: StompSession, connectedHeaders: StompHeaders) {
            val userName = connectedHeaders["user-name"][0]
            println("Connected to WebSocket server. Your username: $userName")
            connectedFuture.complete(session)
            subscribeToGameTopics(session)
            session.send("/app/after_connect", "player connected")
            connectionHandler.invoke(userName)
        }

        override fun handleTransportError(session: StompSession, exception: Throwable) {
            println("Transport error: ${exception.message}")
            if (!session.isConnected) connect()
        }

        override fun handleException(
            session: StompSession,
            command: StompCommand?,
            headers: StompHeaders,
            payload: ByteArray,
            exception: Throwable
        ) {
            println("Exception: ${exception.message}")
        }

        private fun subscribeToGameTopics(session: StompSession) {
            session.subscribe("/user/queue/game_moves", object : StompFrameHandler {
                override fun getPayloadType(headers: StompHeaders): Type = GameMoveDto::class.java
                override fun handleFrame(headers: StompHeaders, payload: Any?) {
                    moveHandler.invoke(payload as GameMoveDto)
                }
            })

            session.subscribe("/user/queue/notifications", object : StompFrameHandler {
                override fun getPayloadType(headers: StompHeaders): Type = NotificationResponseDto::class.java
                override fun handleFrame(headers: StompHeaders, payload: Any?) {
                    notificationHandler.invoke(payload as NotificationResponseDto)
                }
            })

            session.subscribe("/user/queue/errors", object : StompFrameHandler {
                override fun getPayloadType(headers: StompHeaders): Type = ErrorMessageResponseDto::class.java
                override fun handleFrame(headers: StompHeaders, payload: Any?) {
                    errorHandler.invoke(payload as ErrorMessageResponseDto)
                }
            })
        }
    }

    fun getSession(): StompSession = connectedFuture.get()

    fun disconnect() {
        getSession().disconnect()
        println("Disconnected from the WebSocket server.")
    }

    @PreDestroy
    fun onShutDown() {
        if (getSession().isConnected) {
            disconnect()
        }
    }
}
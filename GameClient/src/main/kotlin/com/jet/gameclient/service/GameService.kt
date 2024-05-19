package com.jet.gameclient.service

import com.jet.gameclient.dto.ErrorMessageResponseDto
import com.jet.gameclient.dto.GameMoveDto
import com.jet.gameclient.dto.NotificationResponseDto
import jakarta.annotation.PreDestroy
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.simp.stomp.*
import org.springframework.stereotype.Service
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import java.lang.reflect.Type
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.CompletableFuture


@Service
@Profile("!test")
class GameService(@Value("\${websocket.connection.url}") private val url: String) {

    companion object {
        val VALID_ADDED_VALUES = arrayOf(-1, 0, 1)
    }

    var isManualPlay = true
    var numOfGameMove = 0
    var isFirstToPlay: Boolean = false
    private var userName: String = ""

    @Autowired
    private lateinit var gameServerClient: GameServerClient

    private var connectedFuture = CompletableFuture<StompSession>()

    var gameEndedCallback: (() -> Unit)? = null


    fun startAutomaticGame() {
        isManualPlay = false
        connect()
    }

    fun startManualGame() {
        isManualPlay = true
        connect()
    }

    fun connect() {
        val stompClient = WebSocketStompClient(StandardWebSocketClient())
        stompClient.messageConverter = MappingJackson2MessageConverter()
        connectedFuture = CompletableFuture<StompSession>()

        val future = stompClient.connectAsync(url, object : StompSessionHandlerAdapter() {
            override fun afterConnected(session: StompSession, connectedHeaders: StompHeaders) {
                userName = connectedHeaders["user-name"][0]
                println("Connected to WebSocket server. Your username: $userName")
                connectedFuture.complete(session)
                session.subscribe("/user/queue/game_moves", object : StompFrameHandler {
                    override fun getPayloadType(headers: StompHeaders): Type {
                        return GameMoveDto::class.java
                    }

                    override fun handleFrame(headers: StompHeaders, payload: Any?) {
                        handleMoves(payload as GameMoveDto)
                    }
                })

                session.subscribe("/user/queue/notifications", object : StompFrameHandler {
                    override fun getPayloadType(headers: StompHeaders): Type {
                        return NotificationResponseDto::class.java
                    }

                    override fun handleFrame(headers: StompHeaders, payload: Any?) {
                        handleNotifications(payload as NotificationResponseDto)
                    }
                })

                session.subscribe("/user/queue/errors", object : StompFrameHandler {
                    override fun getPayloadType(headers: StompHeaders): Type {
                        return ErrorMessageResponseDto::class.java
                    }

                    override fun handleFrame(headers: StompHeaders, payload: Any?) {
                        handleErrors(payload as ErrorMessageResponseDto)
                    }
                })

                session.send("/app/after_connect", "player connected")

                if (!isManualPlay) {
                    isFirstToPlay = gameServerClient.isFirstToPlay(userName).isFirstToPlay ?: false

                    if (isFirstToPlay) {
                        generateAutomaticNumber()
                    }
                }
            }

            override fun handleTransportError(session: StompSession, exception: Throwable) {
                println("Transport error: ${exception.message}")
                if (!session.isConnected) {
                    connect()
                }
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
        })

        future.whenComplete { _, ex ->
            if (ex == null) {
                println("Connection established successfully")
            } else {
                println("Failed to connect: ${ex.message}")
            }
        }
    }

    fun getSession(): StompSession {
        return connectedFuture.get()
    }

    fun generateAutomaticNumber(resultingNumberPlusAddedDividedBy3: Int? = null) {
        if (resultingNumberPlusAddedDividedBy3 == 1) {
            return
        }

        var sendNum = 0

        if (numOfGameMove == 0) {
            sendNum = (10..100).random()
        } else {
            for (addedValue in VALID_ADDED_VALUES) {
                if ((resultingNumberPlusAddedDividedBy3!!.plus(addedValue)) % 3 == 0) {

                    sendNum = addedValue
                }
            }

        }

        sendMessage(sendNum)
    }

    fun sendMessage(sendNum: Int) {
        if (numOfGameMove == 0) {
            getSession().send(
                "/app/send",
                GameMoveDto(resultingNumber = sendNum)
            )
        } else {
            getSession().send(
                "/app/send",
                GameMoveDto(added = sendNum)
            )
        }
    }

    fun handleMoves(move: GameMoveDto) {
        ++numOfGameMove

        val resultingNumber = move.resultingNumber
        var added = move.added

        val resultingNumberPlusAdded: Int
        var resultingNumberPlusAddedDividedBy3 = 0

        if (added == null) {
            added = 0

            println(
                """
                 | New Game Move:
                 | timestamp: ${ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)}
                 | resultingNumber: $resultingNumber
                """.trimMargin()
            )
        } else {
            resultingNumberPlusAdded = resultingNumber!!.plus(added)
            resultingNumberPlusAddedDividedBy3 = resultingNumberPlusAdded / 3

            println(
                """
                 | New Game Move:
                 | timestamp: ${ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)}
                 | resultingNumber: $resultingNumber
                 | added: $added
                 | resultingNumberPlusAdded: $resultingNumberPlusAdded
                 | resultingNumberPlusAddedDividedBy3: $resultingNumberPlusAddedDividedBy3
                """.trimMargin()
            )
        }

        if (!isManualPlay) {
            if (isFirstToPlay) {

                if (numOfGameMove % 2 == 0) {

                    generateAutomaticNumber(resultingNumberPlusAddedDividedBy3)
                }
            } else {

                if (numOfGameMove % 2 == 1) {

                    if (resultingNumberPlusAddedDividedBy3 == 0) {
                        resultingNumberPlusAddedDividedBy3 = resultingNumber!!
                    }

                    generateAutomaticNumber(resultingNumberPlusAddedDividedBy3)
                }
            }
        }
    }

    private fun handleNotifications(notification: NotificationResponseDto) {
        when (notification.code) {
            "GAME_IS_BUSY_WITH_TWO_PLAYERS",
            "OTHER_USER_DISCONNECTED",
            "YOU_WON",
            "YOU_LOST" -> {
                println("notification: ${notification.text}")
                disconnect()
                gameEndedCallback?.invoke()
            }
            "WAIT_OTHER_USER_JOIN_GAME" -> {
                if (isManualPlay) {
                    println("notification: ${notification.text}")
                }
            }
        }

        println("notification: $notification")

    }

    private fun handleErrors(error: ErrorMessageResponseDto) {
        println("Error: $error")
    }

    fun disconnect() {
        getSession().disconnect()
        numOfGameMove = 0
        println("Disconnected from the game.")
    }

    /**
     * Unsubscribe and close connection before destroying this instance (e.g. on application shutdown).
     */
    @PreDestroy
    fun onShutDown() {
        if (getSession().isConnected) {
            disconnect()
        }
    }
}
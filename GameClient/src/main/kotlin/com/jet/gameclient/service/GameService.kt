package com.jet.gameclient.service

import com.jet.gameclient.dto.ErrorMessageResponseDto
import com.jet.gameclient.dto.GameMoveDto
import com.jet.gameclient.dto.NotificationResponseDto
import org.springframework.context.annotation.Profile
import org.springframework.messaging.simp.stomp.*
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


@Service
@Profile("!test")
class GameService(private val webSocketService: WebSocketService, private val gameServerClient: GameServerClient) {

    companion object {
        val VALID_ADDED_VALUES = arrayOf(-1, 0, 1)
    }

    private var isManualPlay = true
    private var numOfGameMove = 0
    private var isFirstToPlay = false
    private lateinit var userName: String

    var gameEndedCallback: (() -> Unit)? = null

    init {
        webSocketService.moveHandler = ::handleMoves
        webSocketService.notificationHandler = ::handleNotifications
        webSocketService.errorHandler = ::handleErrors
        webSocketService.connectionHandler = ::onConnected
    }

    fun startAutomaticGame() {
        isManualPlay = false
        webSocketService.connect()
    }

    fun startManualGame() {
        isManualPlay = true
        webSocketService.connect()
    }

    fun playGameMove(sendNum: Int) {
        sendMessage(sendNum)
    }

    private fun onConnected(userName: String) {
        this.userName = userName
        if (!isManualPlay) {
            isFirstToPlay = gameServerClient.isFirstToPlay(userName).isFirstToPlay ?: false
            if (isFirstToPlay) generateAutomaticNumber()
        }
    }

    private fun generateAutomaticNumber(resultingNumberPlusAddedDividedBy3: Int? = null) {
        if (resultingNumberPlusAddedDividedBy3 == 1) return

        val sendNum = when (numOfGameMove) {
            0 -> (10..100).random()
            else -> VALID_ADDED_VALUES.firstOrNull { (resultingNumberPlusAddedDividedBy3!! + it) % 3 == 0 } ?: 0
        }
        sendMessage(sendNum)
    }

    private fun sendMessage(sendNum: Int) {
        val destination = "/app/send"
        val message = if (numOfGameMove == 0) {
            GameMoveDto(resultingNumber = sendNum)
        } else {
            GameMoveDto(added = sendNum)
        }
        webSocketService.getSession().send(destination, message)
    }

    private fun handleMoves(move: GameMoveDto) {
        ++numOfGameMove

        val resultingNumber = move.resultingNumber
        val added = move.added

        val resultingNumberPlusAdded: Int
        var resultingNumberPlusAddedDividedBy3 = 0

        if (added == null) {
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
                webSocketService.disconnect()
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

}
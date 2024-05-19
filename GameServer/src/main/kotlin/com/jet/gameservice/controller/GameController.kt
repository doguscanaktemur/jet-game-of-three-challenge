package com.jet.gameservice.controller

import com.jet.gameservice.domain.Game
import com.jet.gameservice.dto.ErrorMessageResponseDto
import com.jet.gameservice.dto.FirstUserToPlayResponseDto
import com.jet.gameservice.dto.GameMoveDto
import com.jet.gameservice.enums.Notification
import com.jet.gameservice.service.WebSocketMessageSendingService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class GameController {

    @Autowired
    private lateinit var game: Game

    @Autowired
    private lateinit var webSocketMessageSendingService: WebSocketMessageSendingService

    //WebSocket endpoints
    @MessageMapping("/send")
    fun gameMove(gameMoveDto: GameMoveDto, headerAccessor: SimpMessageHeaderAccessor) {
        LOGGER.debug("gameMoveDto={}", gameMoveDto)
        val user = headerAccessor.user?.name
        LOGGER.debug("user= $user")
        game.playGameMoveForUser(user, gameMoveDto)
    }

    @MessageMapping("/after_connect")
    fun afterConnect(headerAccessor: SimpMessageHeaderAccessor) {
        val user = headerAccessor.user?.name
        if (LOGGER.isDebugEnabled) {
            LOGGER.debug("user= $user")
        }
        if (!game.isUserInGame(user)) {
            webSocketMessageSendingService.sendNotificationToUser(user!!, Notification.GAME_IS_BUSY_WITH_TWO_PLAYERS)
        } else {
            game.getFirstResultingNumber(user)
        }
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    fun handleException(t: Throwable, ha: SimpMessageHeaderAccessor): ErrorMessageResponseDto {
        val user = ha.user?.name
        LOGGER.debug("user= $user")
        val errorMessageResponseDto = ErrorMessageResponseDto(t)
        LOGGER.error(errorMessageResponseDto.toString(), t)
        return errorMessageResponseDto
    }

    // Rest endpoint
    @GetMapping("/game/first-player")
    fun isFirstToPlay(@RequestHeader("socketUserName") socketUserName: String): FirstUserToPlayResponseDto {
        if (LOGGER.isDebugEnabled) {
            LOGGER.debug("socketUserName={}", socketUserName)
        }
        val isFirstToPlay: Boolean = game.isFirstToPlay(socketUserName)
        val firstUserToPlayResponseDto = FirstUserToPlayResponseDto(isFirstToPlay)
        if (LOGGER.isDebugEnabled) {
            LOGGER.debug("firstUserToPlayResponseDto={}", firstUserToPlayResponseDto)
        }
        return firstUserToPlayResponseDto
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(GameController::class.java)
    }
}
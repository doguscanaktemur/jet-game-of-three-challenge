package com.jet.gameservice.service

import com.jet.gameservice.dto.NotificationResponseDto
import com.jet.gameservice.enums.Notification
import com.jet.gameservice.model.GameMove
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service


interface WebSocketMessageSendingService {
    fun sendGameMoveToUser(toUser: String, gameMove: GameMove)
    fun sendNotificationToUser(toUser: String, notificationEnum: Notification)
}

@Service
class WebSocketMessageSendingServiceImpl(private val simpMessagingTemplate: SimpMessagingTemplate) : WebSocketMessageSendingService {

    override fun sendGameMoveToUser(toUser: String, gameMove: GameMove) {
        simpMessagingTemplate.convertAndSendToUser(toUser, "/queue/game_moves", gameMove)
    }

    override fun sendNotificationToUser(toUser: String, notificationEnum: Notification) {
        simpMessagingTemplate.convertAndSendToUser(
            toUser,
            "/queue/notifications",
            NotificationResponseDto(notificationEnum)
        )
    }
}
package com.jet.gameservice.dto

import com.jet.gameservice.enums.Notification

class NotificationResponseDto(notificationEnum: Notification) {
    val text: String
    val code: String

    init {
        this.text = notificationEnum.text
        this.code = notificationEnum.code
    }
}
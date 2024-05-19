package com.jet.gameservice.dto

import com.jet.gameservice.enums.Notification

data class NotificationResponseDto(var text: String? = null, var code: String? = null) {
    constructor(notificationEnum: Notification) : this(notificationEnum.text, notificationEnum.code)
}
package com.jet.gameservice.dto

import java.lang.StringBuilder

class FirstUserToPlayResponseDto(val isFirstToPlay: Boolean) {

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("FirstUserToPlayResponseDto [isFirstToPlay=")
        builder.append(isFirstToPlay)
        builder.append("]")
        return builder.toString()
    }
}
package com.jet.gameclient.client

import com.jet.gameclient.dto.FirstUserToPlayResponseDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod


@FeignClient(name = "game-server", url = "localhost:8080")
interface GameServerClient {

    @RequestMapping(method = [RequestMethod.GET], value = ["/game/first-player"])
    fun isFirstToPlay(@RequestHeader("socketUserName") socketUserName: String): FirstUserToPlayResponseDto
}
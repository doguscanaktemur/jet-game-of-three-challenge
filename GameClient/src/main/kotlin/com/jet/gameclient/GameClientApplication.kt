package com.jet.gameclient

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootApplication
@EnableFeignClients
open class GameClientApplication

fun main(args: Array<String>) {
    runApplication<GameClientApplication>(*args)
}

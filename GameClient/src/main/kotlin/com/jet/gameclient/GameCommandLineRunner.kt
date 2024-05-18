package com.jet.gameclient

import com.jet.gameclient.client.WebSocketClient
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.util.*


@Component
class GameCommandLineRunner(private val webSocketClient: WebSocketClient) : CommandLineRunner {

    @Volatile
    private var gameEnded = false

    override fun run(vararg args: String?) {

        val scanner = Scanner(System.`in`)

        println("Welcome to the Game of Three Terminal Application")
        println("Commands:")
        println("manual - Start manual game mode")
        println("automatic - Start automatic game mode")
        println("exit - Exit the application")

        webSocketClient.gameEndedCallback = {
            gameEnded = true
            println("Game ended.")
        }

        promptForCommand(scanner)
    }

    private fun promptForCommand(scanner: Scanner) {
        while (true) {
            print("> ")
            val input = scanner.nextLine().trim()

            when {
                input.equals("manual", ignoreCase = true) -> {
                    println("Manual game mode selected")

                    gameEnded = false
                    webSocketClient.startManualGame()
                    handleManualGame(scanner)
                }
                input.equals("automatic", ignoreCase = true) -> {
                    println("Automatic game mode selected")

                    gameEnded = false
                    webSocketClient.startAutomaticGame()
                }
                input.equals("exit", ignoreCase = true) -> {
                    println("Exiting the application")
                    break
                }
                else -> {
                    println("Invalid command")
                }
            }
        }
    }

    private fun handleManualGame(scanner: Scanner) {
        println("Enter your moves:")

        while (!gameEnded) {
            println("Your move: ")
            val move = scanner.nextLine().trim()
            if (!gameEnded) {
                webSocketClient.sendMessage(move.toInt())
            }
        }
        println("Exiting manual game mode. Please enter your command:")
        promptForCommand(scanner)
    }

}
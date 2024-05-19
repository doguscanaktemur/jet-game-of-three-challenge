package com.jet.gameclient

import com.jet.gameclient.service.GameService
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.*


@Component
@Profile("!test")
class GameCommandLineRunner(private val gameService: GameService) : CommandLineRunner {

    @Volatile
    private var gameEnded = false

    override fun run(vararg args: String?) {

        val scanner = Scanner(System.`in`)

        println("Welcome to the Game of Three Terminal Application")
        println("Commands:")
        println("manual - Start manual game mode")
        println("automatic - Start automatic game mode")
        println("exit - Exit the application")

        gameService.gameEndedCallback = {
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
                    gameService.startManualGame()
                    handleManualGame(scanner)
                }
                input.equals("automatic", ignoreCase = true) -> {
                    println("Automatic game mode selected")

                    gameEnded = false
                    gameService.startAutomaticGame()
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
                gameService.sendMessage(move.toInt())
            }
        }
        println("Exiting manual game mode. Please enter your command:")
        promptForCommand(scanner)
    }

}
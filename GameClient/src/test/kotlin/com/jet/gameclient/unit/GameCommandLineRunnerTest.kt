package com.jet.gameclient.unit

import com.jet.gameclient.GameCommandLineRunner
import com.jet.gameclient.service.GameService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream

internal class GameCommandLineRunnerTest {

    private lateinit var gameCommandLineRunner: GameCommandLineRunner
    private lateinit var gameService: GameService

    @BeforeEach
    fun setUp() {
        gameService = mock(GameService::class.java)
        gameCommandLineRunner = GameCommandLineRunner(gameService)
    }

    @Test
    fun testPromptForCommand_ValidCommands() {
        val inputStream = ByteArrayInputStream("automatic\nexit".toByteArray())
        val outputStream = ByteArrayOutputStream()
        System.setIn(inputStream)
        System.setOut(PrintStream(outputStream))

        gameCommandLineRunner.run()

        val output = outputStream.toString().trim()
        assert(output.contains("Automatic game mode selected"))
        assert(output.contains("Exiting the application"))
    }

    @Test
    fun testPromptForCommand_InvalidCommand() {
        val inputStream = ByteArrayInputStream("invalid\nexit".toByteArray())
        val outputStream = ByteArrayOutputStream()
        System.setIn(inputStream)
        System.setOut(PrintStream(outputStream))

        gameCommandLineRunner.run()

        val output = outputStream.toString().trim()
        assert(output.contains("Invalid command"))
        assert(output.contains("Exiting the application"))
    }

}

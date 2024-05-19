package com.jet.gameservice.unit.controller

import com.jet.gameservice.controller.GameController
import com.jet.gameservice.domain.Game
import com.jet.gameservice.service.WebSocketMessageSendingService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(GameController::class)
class GameControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var game: Game

    @MockBean
    private lateinit var webSocketMessageSendingService: WebSocketMessageSendingService

    @Test
    fun `isFirstToPlay endpoint should return correct response`() {
        val socketUserName = "testUser"
        val isFirstToPlay = true

        `when`(game.isFirstToPlay(socketUserName)).thenReturn(isFirstToPlay)

        mockMvc.perform(
            get("/game/first-player")
                .header("socketUserName", socketUserName)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.firstToPlay").value(isFirstToPlay))
    }
}

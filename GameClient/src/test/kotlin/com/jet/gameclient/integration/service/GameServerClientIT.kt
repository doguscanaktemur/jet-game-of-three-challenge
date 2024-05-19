package com.jet.gameclient.integration.service

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.jet.gameclient.dto.FirstUserToPlayResponseDto
import com.jet.gameclient.service.GameServerClient
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import java.nio.charset.StandardCharsets

@SpringBootTest
@ActiveProfiles("test")
@EnableFeignClients
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GameServerClientIT {

    private val mockGameServer: WireMockServer =  WireMockServer(8080)

    @Autowired
    private lateinit var gameServerClient: GameServerClient

    @BeforeAll
    fun beforeAll() {
        mockGameServer.start()

        mockGameServer.stubFor(
            WireMock.get(WireMock.urlEqualTo("/game/first-player"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(
                            FirstUserToPlayResponseDto::class.java.classLoader.getResourceAsStream("get-firsttoplay-response.json")
                                ?.bufferedReader(StandardCharsets.UTF_8)
                                ?.use { it.readText() }
                                ?: throw IllegalArgumentException("Resource not found: get-firsttoplay-response.json")
                        )
                )
        )
    }

    @AfterAll
    fun afterAll() {
        mockGameServer.stop()
    }

    @BeforeEach
    fun beforeEach() {
        mockGameServer.resetRequests()
    }

    @Test
    fun whenGetFirstToPlay_thenItShouldBeReturnedTrue() {
        assertTrue(gameServerClient.isFirstToPlay("test").isFirstToPlay!!)
    }

}
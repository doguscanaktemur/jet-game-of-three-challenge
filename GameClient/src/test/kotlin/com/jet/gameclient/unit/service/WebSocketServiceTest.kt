package com.jet.gameclient.unit.service

import com.jet.gameclient.dto.ErrorMessageResponseDto
import com.jet.gameclient.dto.GameMoveDto
import com.jet.gameclient.dto.NotificationResponseDto
import com.jet.gameclient.service.WebSocketService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.*
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.springframework.messaging.simp.stomp.StompFrameHandler
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.messaging.simp.stomp.StompSessionHandler
import org.springframework.web.socket.messaging.WebSocketStompClient
import java.util.concurrent.CompletableFuture

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WebSocketServiceTest {

    @Mock
    private lateinit var mockStompClient: WebSocketStompClient

    @Mock
    private lateinit var mockStompSession: StompSession

    @Captor
    private lateinit var stompSessionHandlerCaptor: ArgumentCaptor<StompSessionHandler>

    @Captor
    private lateinit var notificationStompFrameHandlerCaptor: ArgumentCaptor<StompFrameHandler>

    @Captor
    private lateinit var errorStompFrameHandlerCaptor: ArgumentCaptor<StompFrameHandler>

    @Captor
    private lateinit var moveStompFrameHandlerCaptor: ArgumentCaptor<StompFrameHandler>

    private lateinit var webSocketService: WebSocketService

    private val url = "ws://localhost:8080/websocket"

    @BeforeEach
    fun setUp() {
        // Initialize the service with the mock URL
        webSocketService = WebSocketService(url)

        // Mock the STOMP client and its behavior
        `when`(
            mockStompClient.connectAsync(
                any(),
                any()
            )
        ).thenReturn(CompletableFuture.completedFuture(mockStompSession))

        // Replace the internal STOMP client with the mock
        webSocketService = spy(webSocketService).also {
            doReturn(mockStompClient).`when`(it).createStompClient()
        }
    }

    @Test
    fun `test connect success`() {
        webSocketService.connectionHandler = mock()

        // Call the connect method
        webSocketService.connect()

        // Capture the StompSessionHandler used in connectAsync
        verify(mockStompClient).connectAsync(eq(url), stompSessionHandlerCaptor.capture())

        // Simulate a successful connection
        val stompHeaders = StompHeaders().apply { add("user-name", "testUser") }
        stompSessionHandlerCaptor.value.afterConnected(mockStompSession, stompHeaders)

        verify(webSocketService.connectionHandler).invoke("testUser")

        // Verify that the session was properly handled
        assert(webSocketService.getSession() == mockStompSession)
    }

    @Test
    fun `test handleTransportError reconnects`() {
        // Call the connect method
        webSocketService.connect()
        verify(mockStompClient).connectAsync(eq(url), stompSessionHandlerCaptor.capture())

        // Simulate a transport error
        stompSessionHandlerCaptor.value.handleTransportError(mockStompSession, RuntimeException("Transport error"))

        // Verify reconnection attempt
        verify(mockStompClient, times(2)).connectAsync(eq(url), any())
    }

    @Test
    fun `test handleException logs the exception`() {
        // Call the connect method
        webSocketService.connect()
        verify(mockStompClient).connectAsync(eq(url), stompSessionHandlerCaptor.capture())

        // Simulate an exception
        val headers = StompHeaders()
        val payload = ByteArray(0)
        val exception = RuntimeException("Test exception")
        stompSessionHandlerCaptor.value.handleException(mockStompSession, null, headers, payload, exception)

        // Since we're just logging, there's no direct verification here. You'd typically verify logs in an integration test.
    }

    @Test
    fun `test subscription and message handling`() {
        // Setup handlers
        webSocketService.moveHandler = mock()
        webSocketService.notificationHandler = mock()
        webSocketService.errorHandler = mock()
        webSocketService.connectionHandler = mock()

        // Call the connect method
        webSocketService.connect()
        verify(mockStompClient).connectAsync(eq(url), stompSessionHandlerCaptor.capture())

        // Simulate successful connection
        val stompHeaders = StompHeaders().apply { add("user-name", "testUser") }
        stompSessionHandlerCaptor.value.afterConnected(mockStompSession, stompHeaders)

        // Verify subscriptions
        verify(mockStompSession).subscribe(eq("/user/queue/game_moves"), moveStompFrameHandlerCaptor.capture())
        verify(mockStompSession).subscribe(
            eq("/user/queue/notifications"),
            notificationStompFrameHandlerCaptor.capture()
        )
        verify(mockStompSession).subscribe(eq("/user/queue/errors"), errorStompFrameHandlerCaptor.capture())

        // Simulate receiving messages
        val gameMove = GameMoveDto(resultingNumber = 5)
        val notification = NotificationResponseDto(code = "YOU_WON", text = "You won the game!")
        val error = ErrorMessageResponseDto(errorMessage = "An error occurred")

        moveStompFrameHandlerCaptor.value.handleFrame(StompHeaders(), gameMove)
        notificationStompFrameHandlerCaptor.value.handleFrame(StompHeaders(), notification)
        errorStompFrameHandlerCaptor.value.handleFrame(StompHeaders(), error)

        verify(webSocketService.moveHandler).invoke(gameMove)
        verify(webSocketService.notificationHandler).invoke(notification)
        verify(webSocketService.errorHandler).invoke(error)
    }

    @Test
    fun `test disconnect`() {
        // Setup
        `when`(mockStompSession.isConnected).thenReturn(true)
        webSocketService.connectionHandler = mock()

        // Connect and then disconnect
        webSocketService.connect()
        verify(mockStompClient).connectAsync(eq(url), stompSessionHandlerCaptor.capture())
        val stompHeaders = StompHeaders().apply { add("user-name", "testUser") }
        stompSessionHandlerCaptor.value.afterConnected(mockStompSession, stompHeaders)

        webSocketService.disconnect()
        verify(mockStompSession).disconnect()
    }

    @Test
    fun `test onShutDown`() {
        // Setup
        `when`(mockStompSession.isConnected).thenReturn(true)
        webSocketService.connectionHandler = mock()

        // Connect and then shutdown
        webSocketService.connect()
        verify(mockStompClient).connectAsync(eq(url), stompSessionHandlerCaptor.capture())
        val stompHeaders = StompHeaders().apply { add("user-name", "testUser") }
        stompSessionHandlerCaptor.value.afterConnected(mockStompSession, stompHeaders)

        webSocketService.onShutDown()
        verify(mockStompSession).disconnect()
    }
}

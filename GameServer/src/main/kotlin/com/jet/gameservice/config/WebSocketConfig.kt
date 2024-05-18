package com.jet.gameservice.config

import com.jet.gameservice.domain.Game
import com.jet.gameservice.domain.GameImpl
import com.jet.gameservice.handler.CustomHandshakeHandler
import com.jet.gameservice.service.WebSocketMessageSendingService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        //TODO Use RabbitMQ as a Full External STOMP Broker
        registry.enableSimpleBroker("/queue/game_moves", "/queue/notifications", "/queue/errors")

        /*
		 * The application destination prefix `/app` designates the broker to send
		 * messages prefixed with `/app` to our `@MessageMapping`s.
		 */
        registry.setApplicationDestinationPrefixes("/app")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {

        /*
		 * This configures a STOMP (Simple Text Oriented Messaging Protocol)
		 * endpoint for our websocket to be hosted on
		 */
        registry.addEndpoint("/websocket").setHandshakeHandler(CustomHandshakeHandler(game(null)))
    }

    @Bean
    fun game(webSocketMessageSendingService: WebSocketMessageSendingService?): Game {
        return GameImpl(webSocketMessageSendingService)
    }
}
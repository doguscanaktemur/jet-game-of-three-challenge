package com.jet.gameservice.handler

import com.jet.gameservice.domain.Game
import com.jet.gameservice.security.StompPrincipal
import jakarta.servlet.http.HttpSession
import org.slf4j.LoggerFactory
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.support.DefaultHandshakeHandler
import java.security.Principal
import java.util.*
import java.util.Map

/**
 *
 * Custom class for storing principal.
 *
 */
class CustomHandshakeHandler(private val game: Game) : DefaultHandshakeHandler() {

    override fun determineUser(
        request: ServerHttpRequest,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Principal? {
        val servletRequest = request as ServletServerHttpRequest
        val session: HttpSession = servletRequest.servletRequest.session
        var user = session.getAttribute("socketUserName") as String?
        if (user == null) {
            user = UUID.randomUUID().toString()
            session.setAttribute("socketUserName", user)
        }
        game.addUserToGame(user)
        LOGGER.debug("user={}", user)

        // Generate principal with sessionId or integer as name
        return StompPrincipal(user)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(CustomHandshakeHandler::class.java)
    }
}
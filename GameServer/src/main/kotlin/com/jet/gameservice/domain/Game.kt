package com.jet.gameservice.domain

import com.jet.gameservice.dto.GameMoveDto
import com.jet.gameservice.enums.Error
import com.jet.gameservice.enums.Notification
import com.jet.gameservice.exception.GameException
import com.jet.gameservice.model.GameMove
import com.jet.gameservice.service.WebSocketMessageSendingService

interface Game {
    fun addUserToGame(user: String?)
    fun playGameMoveForUser(user: String?, gameMoveDto: GameMoveDto)
    fun removeUserFromGame(user: String?)
    fun getFirstResultingNumber(user: String?)
    fun isUserInGame(user: String?): Boolean
    fun isFirstToPlay(user: String?): Boolean
}

class GameImpl(private val webSocketMessageSendingService: WebSocketMessageSendingService?) : Game {
    @Volatile
    private var user1: String? = null

    @Volatile
    private var user2: String? = null

    @Volatile
    private var nextUserTurn: String? = null

    @Volatile
    private var numOfUsers = 0

    @Volatile
    private var numOfGameMove = 0

    @Volatile
    private var lastResultingNumber = 0

    @Synchronized
    override fun addUserToGame(user: String?) {
        if (numOfUsers == 2) {
            return
        }
        if (user1 == null) {
            user1 = user
            nextUserTurn = user
            ++numOfUsers
        } else if (user2 == null) {
            user2 = user
            if (numOfGameMove == 1) {
                nextUserTurn = user
            }
            ++numOfUsers
        }
    }

    @Synchronized
    override fun playGameMoveForUser(user: String?, gameMoveDto: GameMoveDto) {
        if (user == nextUserTurn == false) {
            throw GameException(Error.NOT_YOUR_TURN)
        }
        val gameMove = GameMove(gameMoveDto)
        val otherUser = getOtherUser(user)
        if (numOfGameMove == 0) { // if first move
            if (gameMove.resultingNumber == null) {
                throw GameException(Error.PARAMETER_IS_NULL, "resultingNumber")
            }
            lastResultingNumber = gameMove.resultingNumber!!
            gameMove.added = null
        } else { // if not first move
            if (gameMove.added == null) {
                throw GameException(Error.PARAMETER_IS_NULL, "added")
            }
            if ((lastResultingNumber + gameMove.added!!) % 3 != 0) {
                throw GameException(Error.NOT_DIVISIBLE_BY_3, lastResultingNumber + gameMove.added!!)
            }
            gameMove.resultingNumber = lastResultingNumber
            lastResultingNumber += gameMove.added!!
            lastResultingNumber /= 3
        }
        if (otherUser != null) {
            webSocketMessageSendingService!!.sendGameMoveToUser(otherUser, gameMove)
        }
        webSocketMessageSendingService!!.sendGameMoveToUser(user!!, gameMove)
        if (lastResultingNumber == 1) {
            webSocketMessageSendingService.sendNotificationToUser(user, Notification.YOU_WON)
            if (otherUser != null) {
                webSocketMessageSendingService.sendNotificationToUser(otherUser, Notification.YOU_LOST)
            }
            reset()
        } else {
            nextUserTurn = otherUser
            ++numOfGameMove
        }
    }

    private fun getOtherUser(user: String?): String? {
        return if (user == user1) user2 else user1
    }

    private fun reset() {
        user1 = null
        user2 = null
        nextUserTurn = null
        numOfUsers = 0
        numOfGameMove = 0
        lastResultingNumber = 0
    }

    @Synchronized
    override fun removeUserFromGame(user: String?) {
        if (isUserInGame(user)) {
            val otherUser = getOtherUser(user)
            if (otherUser != null) {
                webSocketMessageSendingService!!.sendNotificationToUser(otherUser, Notification.OTHER_USER_DISCONNECTED)
            }
            reset()
        }
    }

    override fun getFirstResultingNumber(user: String?) {
        if (user == null) {
            throw GameException(Error.PARAMETER_IS_NULL, "user")
        }
        if (numOfGameMove > 0) {
            webSocketMessageSendingService!!.sendGameMoveToUser(user, GameMove(lastResultingNumber, null))
        }
        if (numOfUsers == 1) {
            webSocketMessageSendingService!!.sendNotificationToUser(user, Notification.WAIT_OTHER_USER_JOIN_GAME)
        }
    }

    override fun isUserInGame(user: String?): Boolean {
        if (user == null) {
            throw GameException(Error.PARAMETER_IS_NULL, "user")
        }
        return user == user1 || user == user2
    }

    override fun isFirstToPlay(user: String?): Boolean {
        if (user == null) {
            throw GameException(Error.PARAMETER_IS_NULL, "user")
        }
        return user == user1
    }
}
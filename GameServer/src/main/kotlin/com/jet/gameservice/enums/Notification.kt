package com.jet.gameservice.enums

enum class Notification(val text: String, val code: String) {
    OTHER_USER_DISCONNECTED(
        "The game is over, because the other player disconnected.",
        "OTHER_USER_DISCONNECTED"
    ),
    WAIT_OTHER_USER_JOIN_GAME(
        "Please wait for another user to join the game. You can still send your first Number anyway.",
        "WAIT_OTHER_USER_JOIN_GAME"
    ),
    YOU_WON(
        "The game is over, you are the winner.",
        "YOU_WON"
    ),
    YOU_LOST(
        "The game is over, you lost.",
        "YOU_LOST"
    ),
    GAME_IS_BUSY_WITH_TWO_PLAYERS(
        "The game is busy with two players. Please try again later",
        "GAME_IS_BUSY_WITH_TWO_PLAYERS"
    )

}
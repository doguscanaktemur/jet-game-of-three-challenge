package com.jet.gameservice.security

import java.security.Principal

class StompPrincipal(private val name: String) : Principal {
    override fun getName(): String {
        return name
    }
}
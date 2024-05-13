package com.restaurant.be.common.principal

import java.security.Principal

object PrincipalUtils {
    private const val ANONYMOUS_USER = "anonymous"

    fun getUsername(principal: Principal?): String {
        return principal?.name ?: ANONYMOUS_USER
    }

    fun isAnonymous(email: String): Boolean {
        return email == ANONYMOUS_USER
    }
}

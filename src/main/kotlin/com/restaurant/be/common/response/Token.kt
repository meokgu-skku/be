package com.restaurant.be.common.response

import java.util.Date

data class Token(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Long,
    val issuedAt: Date
) {

    constructor() : this(
        accessToken = "",
        refreshToken = "",
        tokenType = "",
        expiresIn = 0,
        issuedAt = Date()
    )
}

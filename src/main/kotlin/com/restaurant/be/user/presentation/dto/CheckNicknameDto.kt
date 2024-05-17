@file:Suppress("ktlint", "MatchingDeclarationName")

package com.restaurant.be.user.presentation.dto

data class CheckNicknameResponse(
    val isDuplicate: Boolean
)

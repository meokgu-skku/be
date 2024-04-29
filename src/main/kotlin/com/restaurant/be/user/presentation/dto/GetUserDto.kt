@file:Suppress("ktlint", "MatchingDeclarationName")

package com.restaurant.be.user.presentation.dto

import com.restaurant.be.user.presentation.dto.common.UserDto
import io.swagger.v3.oas.annotations.media.Schema

data class GetUserResponse(
    @Schema(description = "유저 정보")
    val userDto: UserDto
)

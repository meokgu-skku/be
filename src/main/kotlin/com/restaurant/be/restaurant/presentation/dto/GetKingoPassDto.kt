@file:Suppress("ktlint", "MatchingDeclarationName")

package com.restaurant.be.restaurant.presentation.dto

import com.restaurant.be.restaurant.presentation.dto.common.RestaurantDto
import io.swagger.v3.oas.annotations.media.Schema

data class GetKingoPassResponse(
    @Schema(description = "킹고패스 식당 리스트")
    val restaurants: List<RestaurantDto>
)

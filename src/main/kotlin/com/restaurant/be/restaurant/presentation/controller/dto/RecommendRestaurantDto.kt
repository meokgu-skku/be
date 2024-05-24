@file:Suppress("ktlint", "MatchingDeclarationName")

package com.restaurant.be.restaurant.presentation.controller.dto

import com.restaurant.be.restaurant.presentation.controller.dto.common.RestaurantDto
import io.swagger.v3.oas.annotations.media.Schema

data class RecommendRestaurantResponse(
    @Schema(description = "gpt기반 추천 식당 리스트")
    val restaurants: List<RestaurantDto>
)

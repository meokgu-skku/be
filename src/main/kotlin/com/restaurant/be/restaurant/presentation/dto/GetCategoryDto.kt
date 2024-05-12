@file:Suppress("ktlint", "MatchingDeclarationName")

package com.restaurant.be.restaurant.presentation.dto

import com.restaurant.be.restaurant.presentation.dto.common.RestaurantDto
import io.swagger.v3.oas.annotations.media.Schema


data class GetCategoryRequest(
    @Schema(description = "유저 아이디")
    val userId: Long,
    @Schema(description = "카테고리 이름")
    val category: String
)

data class GetCategoryResponse(
    @Schema(description = "카테고리 리스트")
    val categories: List<RestaurantDto>?
)

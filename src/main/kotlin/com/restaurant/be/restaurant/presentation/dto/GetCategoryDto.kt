@file:Suppress("ktlint", "MatchingDeclarationName")

package com.restaurant.be.restaurant.presentation.dto

import com.restaurant.be.restaurant.presentation.dto.common.CategoryDto
import io.swagger.v3.oas.annotations.media.Schema


data class GetCategoryResponse(
    @Schema(description = "카테고리 리스트")
    val categories: List<CategoryDto>
)

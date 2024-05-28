@file:Suppress("ktlint", "MatchingDeclarationName")

package com.restaurant.be.category.presentation.controller.dto

import com.restaurant.be.category.presentation.controller.dto.common.CategoryDto
import io.swagger.v3.oas.annotations.media.Schema

data class GetCategoriesResponse(
    @Schema(description = "카테고리 리스트")
    val categories: List<CategoryDto>
)

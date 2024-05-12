package com.restaurant.be.restaurant.presentation.dto.common

import io.swagger.v3.oas.annotations.media.Schema

data class CategoryDto(
    @Schema(description = "카테고리 id")
    val id: Long,

    @Schema(description = "카테고리 이름")
    val name: String
)

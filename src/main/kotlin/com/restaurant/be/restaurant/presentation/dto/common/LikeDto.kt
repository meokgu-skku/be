package com.restaurant.be.restaurant.presentation.dto.common

import io.swagger.v3.oas.annotations.media.Schema

data class LikeDto(
    @Schema(description = "레스토랑 id")
    val restaurantId: Long,

    @Schema(description = "유저 id")
    val userId: Long,

    @Schema(description = "좋아요한 여부")
    val isLike: Boolean
)

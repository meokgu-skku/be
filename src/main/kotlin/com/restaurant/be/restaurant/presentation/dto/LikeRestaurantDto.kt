package com.restaurant.be.restaurant.presentation.dto

import com.restaurant.be.restaurant.presentation.dto.common.RestaurantDto
import io.swagger.annotations.ApiModelProperty
import io.swagger.v3.oas.annotations.media.Schema

data class GetLikeRestaurantsResponse(
    @Schema(description = "좋아요한 식당 리스트")
    val restaurants: List<RestaurantDto>
)

data class LikeRestaurantRequest(
    @ApiModelProperty(value = "현재 좋아요 했는지 여부", example = "false", required = true)
    val isLike: Boolean
)
data class LikeRestaurantResponse(
    @Schema(description = "좋아요한 식당 정보")
    val restaurant: RestaurantDto
)

package com.restaurant.be.restaurant.presentation.dto

import com.restaurant.be.restaurant.presentation.dto.common.RestaurantDto
import io.swagger.annotations.ApiModelProperty
import io.swagger.v3.oas.annotations.media.Schema

data class GetRestaurantRequest(
    @ApiModelProperty(value = "유저 아아디", example = "1", required = false)
    val userId: Long
)

data class GetRestaurantsResponse(
    @Schema(description = "식당 리스트")
    val restaurants: List<RestaurantDto>
)

data class GetRestaurantResponse(
    @Schema(description = "식당 정보")
    val restaurant: RestaurantDto
)

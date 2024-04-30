package com.restaurant.be.restaurant.presentation.dto

import com.restaurant.be.restaurant.presentation.dto.common.RestaurantDto
import io.swagger.annotations.ApiModelProperty
import io.swagger.v3.oas.annotations.media.Schema

data class GetRestaurantRequest(
    @ApiModelProperty(value = "식당 이름 검색", example = "맛집", required = false)
    val query: String,
    @ApiModelProperty(value = "카테고리 필터", example = "[1, 2, 3]", required = false)
    val categoryIds: List<Long>,
    @ApiModelProperty(value = "킹고패스 할인 여부 필터", example = "false", required = false)
    val discountForSkku: Boolean,
    @ApiModelProperty(value = "평점 필터", example = "4.5", required = false)
    val rating: Long,
    @ApiModelProperty(value = "리뷰 개수 필터", example = "100", required = false)
    val reviewCount: Long
)

data class GetRestaurantsResponse(
    @Schema(description = "식당 리스트")
    val restaurants: List<RestaurantDto>
)

data class GetRestaurantResponse(
    @Schema(description = "식당 정보")
    val restaurant: RestaurantDto
)

package com.restaurant.be.restaurant.presentation.dto

import com.restaurant.be.restaurant.presentation.dto.common.RestaurantDto
import io.swagger.annotations.ApiModelProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.Page

data class GetRestaurantsRequest(
    @ApiModelProperty(value = "식당 이름 검색", example = "맛집", required = false)
    val query: String?,
    @ApiModelProperty(value = "카테고리 필터", example = "['1', '2']", required = false)
    val categories: List<String>?,
    @ApiModelProperty(value = "킹고패스 할인 여부 필터", example = "false", required = false)
    val discountForSkku: Boolean?,
    @ApiModelProperty(value = "평점 필터", example = "4.5", required = false)
    val ratingAvg: Double?,
    @ApiModelProperty(value = "리뷰 개수 필터", example = "100", required = false)
    val reviewCount: Int?,
    @ApiModelProperty(value = "최소 가격 필터", example = "10000", required = false)
    val priceMin: Int?,
    @ApiModelProperty(value = "최대 가격 필터", example = "30000", required = false)
    val priceMax: Int?,
    @ApiModelProperty(value = "정렬 기준", example = "BASIC", required = false)
    val sort: Sort = Sort.BASIC,

    @ApiModelProperty(value = "네이버 평점 필터", example = "4.5", required = false)
    val naverRatingAvg: Double?,
    @ApiModelProperty(value = "네이버 리뷰 개수 필터", example = "100", required = false)
    val naverReviewCount: Int?,

    @ApiModelProperty(value = "찜 필터", example = "false", required = false)
    val like: Boolean?
)

enum class Sort {
    BASIC,
    CLOSELY_DESC,
    RATING_DESC,
    REVIEW_COUNT_DESC,
    LIKE_COUNT_DESC
}

data class GetRestaurantsResponse(
    @Schema(description = "식당 리스트")
    val restaurants: Page<RestaurantDto>
)

data class GetRestaurantResponse(
    @Schema(description = "식당 정보")
    val restaurant: RestaurantDto
)

package com.restaurant.be.review.presentation.dto.common

import io.swagger.v3.oas.annotations.media.Schema

data class ReviewRequestDto(
    @Schema(description = "리뷰 id")
    val id: Long,
    @Schema(description = "유저 id")
    val userId: Long,
    @Schema(description = "식당 id")
    val restaurantId: Long,
    @Schema(description = "평가 점수")
    val rating: Int,
    @Schema(description = "리뷰 내용")
    val comment: String,
    @Schema(description = "이미지 url 리스트")
    val imageUrls: List<String>,
    @Schema(description = "좋아요 여부")
    val isLike: Boolean
)

data class ReviewResponseDto(
    @Schema(description = "유저 id")
    val userId: Long,
    @Schema(description = "식당 id")
    val restaurantId: Long,
    @Schema(description = "평가 점수")
    val rating: Int,
    @Schema(description = "리뷰 내용")
    val comment: String,
    @Schema(description = "이미지 url 리스트")
    val imageUrls: List<String>,
    @Schema(description = "좋아요 여부")
    val isLike: Boolean
)

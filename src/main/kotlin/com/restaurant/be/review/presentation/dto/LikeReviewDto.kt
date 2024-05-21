package com.restaurant.be.review.presentation.dto

import com.restaurant.be.review.domain.entity.ReviewLike
import com.restaurant.be.review.presentation.dto.common.ReviewResponseDto
import io.swagger.annotations.ApiModelProperty
import io.swagger.v3.oas.annotations.media.Schema

data class LikeReviewRequest(
    @ApiModelProperty(value = "리뷰 좋아요 여부", required = true)
    val isLike: Boolean
) {
    fun toEntity(userId: Long, reviewId: Long): ReviewLike {
        return ReviewLike(
            userId = userId,
            reviewId = reviewId
        )
    }
}

data class LikeReviewResponse(
    @Schema(description = "리뷰 정보")
    val review: ReviewResponseDto
)

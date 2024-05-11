package com.restaurant.be.review.presentation.dto

import com.restaurant.be.review.domain.entity.Review
import com.restaurant.be.review.presentation.dto.common.ReviewRequestDto
import io.swagger.annotations.ApiModelProperty

data class CreateReviewRequest(
    @ApiModelProperty(value = "리뷰 정보", required = true)
    val review: ReviewRequestDto
)

data class CreateReviewResponse(
    @ApiModelProperty(value = "리뷰 정보", required = true)
    val review: Review
)

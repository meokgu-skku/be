@file:Suppress("ktlint", "MatchingDeclarationName")

package com.restaurant.be.review.presentation.dto

import com.restaurant.be.review.presentation.dto.common.ReviewResponseDto
import io.swagger.annotations.ApiModelProperty

data class CreateReviewResponse(
    @ApiModelProperty(value = "리뷰 정보", required = true)
    val review: ReviewResponseDto
)

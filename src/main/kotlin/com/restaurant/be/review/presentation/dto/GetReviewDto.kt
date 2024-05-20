@file:Suppress("ktlint", "MatchingDeclarationName")

package com.restaurant.be.review.presentation.dto

import com.restaurant.be.review.presentation.dto.common.ReviewResponseDto
import io.swagger.v3.oas.annotations.media.Schema

data class GetReviewsResponse(
    @Schema(description = "리뷰 리스트")
    val reviews: List<ReviewResponseDto>
)

data class GetReviewResponse(
    @Schema(description = "리뷰 단건")
    val review: ReviewResponseDto
)

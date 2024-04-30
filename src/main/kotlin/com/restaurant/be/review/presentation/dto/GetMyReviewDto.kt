@file:Suppress("ktlint", "MatchingDeclarationName")

package com.restaurant.be.review.presentation.dto

import com.restaurant.be.review.presentation.dto.common.ReviewResponseDto
import io.swagger.v3.oas.annotations.media.Schema

data class GetMyReviewResponse(
    @Schema(description = "리뷰 리스트")
    val reviews: List<ReviewResponseDto>
)

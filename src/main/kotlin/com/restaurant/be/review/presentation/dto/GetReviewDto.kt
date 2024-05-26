@file:Suppress("ktlint", "MatchingDeclarationName")

package com.restaurant.be.review.presentation.dto

import com.restaurant.be.review.presentation.dto.common.ReviewResponseDto
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

data class GetReviewsResponse(
    @Schema(description = "리뷰 리스트")
    val reviews: Page<ReviewResponseDto>,
)

data class GetReviewResponse(
    @Schema(description = "리뷰 단건")
    val review: ReviewResponseDto
)

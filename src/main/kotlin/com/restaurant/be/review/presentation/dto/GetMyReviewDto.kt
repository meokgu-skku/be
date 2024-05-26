@file:Suppress("ktlint", "MatchingDeclarationName")

package com.restaurant.be.review.presentation.dto

import com.restaurant.be.review.presentation.dto.common.ReviewResponseDto
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.Pageable

data class GetMyReviewsResponse(
    @Schema(description = "내가 작성한 리뷰 리스트")
    val reviews: List<ReviewResponseDto>,
    @Schema(description = "Pagination 정보")
    val pageable: Pageable
)

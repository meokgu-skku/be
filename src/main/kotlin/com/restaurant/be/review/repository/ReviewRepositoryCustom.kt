package com.restaurant.be.review.repository

import com.restaurant.be.review.presentation.dto.ReviewWithLikesDto
import com.restaurant.be.user.domain.entity.User
import org.springframework.data.domain.Pageable

interface ReviewRepositoryCustom {
    fun findReview(
        user: User,
        reviewId: Long
    ): ReviewWithLikesDto?

    fun findReviews(
        user: User,
        pageable: Pageable
    ): List<ReviewWithLikesDto>

    fun findMyReviews(
        user: User,
        pageable: Pageable
    ): List<ReviewWithLikesDto>
}

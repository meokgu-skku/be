package com.restaurant.be.review.repository

import com.restaurant.be.review.presentation.dto.ReviewWithLikesDto
import com.restaurant.be.user.domain.entity.User

interface ReviewRepositoryCustom {
    fun findReview(
        user: User,
        reviewId: Long
    ): ReviewWithLikesDto?
}

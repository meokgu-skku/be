package com.restaurant.be.review.repository

import com.restaurant.be.review.domain.entity.Review
import com.restaurant.be.review.presentation.dto.ReviewWithLikesDto
import com.restaurant.be.user.domain.entity.User

interface ReviewRepositoryCustom {
    fun findReview(
        user: User,
        review: Review
    ): ReviewWithLikesDto?
}

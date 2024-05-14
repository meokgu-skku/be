package com.restaurant.be.review.presentation.dto

import com.restaurant.be.review.domain.entity.Review

data class ReviewWithLikesDto(
    val review: Review,
    val isLikedByUser: Boolean
)

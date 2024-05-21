package com.restaurant.be.review.repository

import com.restaurant.be.review.domain.entity.ReviewLike
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReviewLikesRepository : JpaRepository<ReviewLike, Long> {
    fun existsByReviewIdAndUserId(reviewId: Long?, userId: Long?): Boolean
    fun deleteByReviewIdAndUserId(reviewId: Long?, userId: Long?)
}

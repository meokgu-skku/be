package com.restaurant.be.review.repository

import com.restaurant.be.review.domain.entity.ReviewLikes
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReviewLikesRepository : JpaRepository<ReviewLikes, Long> {
    fun existsByReviewIdAndUserId(reviewId: Long?, userId: Long?): Boolean
    fun deleteByReviewIdAndUserId(reviewId: Long?, userId: Long?)

}

package com.restaurant.be.review.repository

import com.restaurant.be.review.domain.entity.Review
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReviewRepository : JpaRepository<Review, Long> {
    fun findByUserId(userId: Long?, pageable: Pageable) : Page<Review>
}

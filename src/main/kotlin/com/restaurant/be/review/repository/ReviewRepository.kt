package com.restaurant.be.review.repository

import com.restaurant.be.review.domain.entity.Review
import org.springframework.data.jpa.repository.JpaRepository

interface ReviewRepository : JpaRepository<Review, Long>, ReviewRepositoryCustom
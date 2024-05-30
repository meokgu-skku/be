package com.restaurant.be.common.util

import com.restaurant.be.review.domain.entity.Review
import com.restaurant.be.review.domain.entity.ReviewImage
import com.restaurant.be.user.domain.entity.User

object ReviewUtil {
    fun generateReviewEntity(
        id: Long = 0,
        user: User,
        restaurantId: Long = 0,
        content: String = "default_content",
        rating: Double = 0.0,
        likeCount: Long = 0,
        viewCount: Long = 0,
        images: MutableList<ReviewImage> = mutableListOf()
    ): Review {
        return Review(
            id = id,
            user = user,
            restaurantId = restaurantId,
            content = content,
            rating = rating,
            likeCount = likeCount,
            viewCount = viewCount,
            images = images
        )
    }
}

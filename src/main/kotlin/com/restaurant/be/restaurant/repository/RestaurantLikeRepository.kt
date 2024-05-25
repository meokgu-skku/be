package com.restaurant.be.restaurant.repository

import com.restaurant.be.restaurant.domain.entity.RestaurantLike
import org.springframework.data.jpa.repository.JpaRepository

interface RestaurantLikeRepository : JpaRepository<RestaurantLike, Long> {
    fun deleteByUserIdAndRestaurantId(userId: Long, restaurantId: Long)
}

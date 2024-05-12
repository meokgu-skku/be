package com.restaurant.be.restaurant.presentation.repository

import com.restaurant.be.restaurant.presentation.domain.entity.RestaurantLikes
import org.springframework.data.jpa.repository.JpaRepository

interface RestaurantLikesRepository : JpaRepository<RestaurantLikes, Int> {

    fun findByRestaurantIdAndUserId(restaurantId: Long, userId: Long): RestaurantLikes?

    fun findByUserIdAndLikeTrue(userId: Long): List<RestaurantLikes>?

    fun findByUserIdAndRestaurantIdAndLikeTrue(userId: Long, restaurantId: Long): RestaurantLikes?
}

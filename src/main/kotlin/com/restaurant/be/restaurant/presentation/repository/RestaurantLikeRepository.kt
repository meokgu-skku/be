package com.restaurant.be.restaurant.presentation.repository

import com.restaurant.be.restaurant.presentation.domain.entity.RestaurantLike
import org.springframework.data.jpa.repository.JpaRepository

interface RestaurantLikeRepository : JpaRepository<RestaurantLike, Int> {

    // 해당 유저가 좋아요한 식당 리스트 반환
    fun findByUserId(userId: String): List<RestaurantLike>?

    // 해당 유저가 해당 식당에 좋아요 했는지 확인
    fun findByUserIdAndRestaurantId(userId: String, restaurantId: Long): RestaurantLike?
}

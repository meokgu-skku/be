package com.restaurant.be.restaurant.repository

import com.restaurant.be.restaurant.presentation.domain.entity.RestaurantLike
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface RestaurantLikeRepository : JpaRepository<RestaurantLike, Long> {

    // 해당 유저가 좋아요한 식당 리스트 반환
    fun findByUserId(userId: Long?, pageable: Pageable): Page<RestaurantLike>?

    fun deleteByUserIdAndRestaurantId(userId: Long, restaurantId: Long)
}

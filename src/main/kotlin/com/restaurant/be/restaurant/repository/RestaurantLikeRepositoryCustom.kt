package com.restaurant.be.restaurant.repository

import com.restaurant.be.restaurant.presentation.controller.dto.common.RestaurantDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RestaurantLikeRepositoryCustom {
    fun findRestaurantLikesByUserId(userId: Long, pageable: Pageable): Page<RestaurantDto>
}

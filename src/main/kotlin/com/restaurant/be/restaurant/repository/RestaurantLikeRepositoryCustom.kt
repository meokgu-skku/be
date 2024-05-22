package com.restaurant.be.restaurant.repository

import com.restaurant.be.restaurant.presentation.dto.common.RestaurantDto
import org.springframework.data.domain.Pageable

interface RestaurantLikeRepositoryCustom {
    fun findRestaurantLikesByUserId(userId: Long?, pageable: Pageable): List<RestaurantDto>
}

package com.restaurant.be.restaurant.repository

import com.restaurant.be.restaurant.repository.dto.RestaurantProjectionDto

interface RestaurantRepositoryCustom {
    fun findDtoById(restaurantId: Long): RestaurantProjectionDto?
}

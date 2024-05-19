package com.restaurant.be.restaurant.repository

import com.restaurant.be.restaurant.presentation.domain.entity.Restaurant

interface RestaurantRepository {
    fun findById(id: Long): Restaurant?
}

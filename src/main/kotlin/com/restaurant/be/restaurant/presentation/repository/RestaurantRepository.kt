package com.restaurant.be.restaurant.presentation.repository

import com.restaurant.be.restaurant.presentation.domain.entity.Restaurants
import org.springframework.data.jpa.repository.JpaRepository

interface RestaurantRepository : JpaRepository<Restaurants, Int> {

    fun findById(id: Long): Restaurants?

    fun findByCustomCategoryContaining(customCategory: String): List<Restaurants>?
}

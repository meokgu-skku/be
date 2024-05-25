package com.restaurant.be.restaurant.repository

import com.restaurant.be.restaurant.domain.entity.RestaurantCategory
import org.springframework.data.jpa.repository.JpaRepository

interface RestaurantCategoryRepository : JpaRepository<RestaurantCategory, Long>

package com.restaurant.be.restaurant.repository

import com.restaurant.be.restaurant.domain.entity.Restaurant
import org.springframework.data.jpa.repository.JpaRepository

interface RestaurantRepository : JpaRepository<Restaurant, Long>, RestaurantRepositoryCustom

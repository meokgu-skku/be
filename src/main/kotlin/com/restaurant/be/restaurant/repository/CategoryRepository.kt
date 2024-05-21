package com.restaurant.be.restaurant.repository

import com.restaurant.be.restaurant.presentation.domain.entity.Category
import org.springframework.data.jpa.repository.JpaRepository

interface CategoryRepository : JpaRepository<Category, Long>

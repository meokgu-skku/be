package com.restaurant.be.category.repository

import com.restaurant.be.category.domain.entity.Category
import org.springframework.data.jpa.repository.JpaRepository

interface CategoryRepository : JpaRepository<Category, Long>

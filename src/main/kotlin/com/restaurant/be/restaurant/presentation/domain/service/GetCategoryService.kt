package com.restaurant.be.restaurant.presentation.domain.service

import com.restaurant.be.restaurant.presentation.dto.GetCategoryResponse
import com.restaurant.be.restaurant.presentation.dto.common.CategoryDto
import com.restaurant.be.restaurant.presentation.repository.RestaurantLikeRepository
import com.restaurant.be.restaurant.presentation.repository.RestaurantRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetCategoryService(
    private val restaurantRepository: RestaurantRepository,
    private val restaurantLikeRepository: RestaurantLikeRepository
) {
    @Transactional
    fun getCategories(): GetCategoryResponse {
        // assuming you have the findByCategory method in your repository
        val categories: List<String> = restaurantRepository.findDistinctCustomCategories()
        val categoryDtos: List<CategoryDto> = categories.map{ CategoryDto( 0, it) }

        return GetCategoryResponse(categoryDtos)
    }
}

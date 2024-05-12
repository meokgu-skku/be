package com.restaurant.be.restaurant.presentation.domain.service

import com.restaurant.be.restaurant.presentation.domain.entity.RestaurantLikes
import com.restaurant.be.restaurant.presentation.domain.entity.Restaurants
import com.restaurant.be.restaurant.presentation.dto.GetCategoryRequest
import com.restaurant.be.restaurant.presentation.dto.GetCategoryResponse
import com.restaurant.be.restaurant.presentation.dto.common.RestaurantDto
import com.restaurant.be.restaurant.presentation.repository.RestaurantLikesRepository
import com.restaurant.be.restaurant.presentation.repository.RestaurantsRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetCategoryService(
    private val restaurantsRepository: RestaurantsRepository,
    private val restaurantLikesRepository: RestaurantLikesRepository
) {
    @Transactional
    fun getCategory(getCategoryRequest: GetCategoryRequest): GetCategoryResponse {
        val categoryName: String = getCategoryRequest.category
        // assuming you have the findByCategory method in your repository
        val restaurants: List<Restaurants>? = restaurantsRepository.findByCustomCategoryContaining(categoryName)
        val restaurantDtos: List<RestaurantDto>? = restaurants?.map { it.toDto() }

        restaurantDtos?.forEach { restaurantDto ->
            val isLiked: RestaurantLikes? =
                restaurantLikesRepository.findByUserIdAndRestaurantIdAndLikeTrue(getCategoryRequest.userId, restaurantDto.id)
            if (isLiked != null) {
                restaurantDto.isLike = true
            }
        }

        return GetCategoryResponse(restaurantDtos)
    }
}

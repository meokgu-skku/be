package com.restaurant.be.restaurant.presentation.domain.service

import com.restaurant.be.restaurant.presentation.domain.entity.Restaurants
import com.restaurant.be.restaurant.presentation.dto.GetCategoryResponse
import com.restaurant.be.restaurant.presentation.dto.common.RestaurantDto
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
    fun getCategory(userName: String): GetCategoryResponse {
        // assuming you have the findByCategory method in your repository
        val restaurants: List<Restaurants> = restaurantRepository.findAll()
        val restaurantDtos: List<RestaurantDto> = restaurants.map { it.toDto() }

        restaurantDtos.forEach { restaurantDto ->
            // 해당 식당에 유저가 좋아요 한 여부를 확인
            restaurantDto.isLike = restaurantLikeRepository
                .findByUserNameAndRestaurantId(userName, restaurantDto.id)?.let { true } ?: false
        }

        return GetCategoryResponse(restaurantDtos)
    }
}

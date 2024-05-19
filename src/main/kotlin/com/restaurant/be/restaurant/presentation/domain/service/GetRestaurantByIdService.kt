package com.restaurant.be.restaurant.presentation.domain.service

import com.restaurant.be.common.exception.NotFoundRestaurantException
import com.restaurant.be.restaurant.presentation.domain.entity.Restaurant
import com.restaurant.be.restaurant.presentation.dto.GetRestaurantResponse
import com.restaurant.be.restaurant.repository.RestaurantLikeRepository
import com.restaurant.be.restaurant.repository.RestaurantRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetRestaurantByIdService(
    private val restaurantRepository: RestaurantRepository,
    private val restaurantLikeRepository: RestaurantLikeRepository
) {
    @Transactional(readOnly = true)
    fun getRestaurantById(restaurantId: Long, email: String): GetRestaurantResponse {
        // id가 일치하는 Restaurants Entity 를 가져움. 없으면 return status 404
        val restaurant: Restaurant = restaurantRepository.findById(restaurantId) ?: throw NotFoundRestaurantException()
        return GetRestaurantResponse(restaurant.toDto())
    }
}

package com.restaurant.be.restaurant.service

import com.restaurant.be.restaurant.presentation.dto.GetRestaurantsRequest
import com.restaurant.be.restaurant.presentation.dto.GetRestaurantsResponse
import com.restaurant.be.restaurant.repository.RestaurantEsRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class GetRestaurantService(
    private val restaurantEsRepository: RestaurantEsRepository
) {

    fun getRestaurants(request: GetRestaurantsRequest, pageable: Pageable): GetRestaurantsResponse {
        val restaurants = restaurantEsRepository.searchRestaurants(request, pageable)
        return GetRestaurantsResponse(
            restaurants = restaurants.map { it.toDto() }
        )
    }
}

package com.restaurant.be.restaurant.presentation.domain.service

import com.restaurant.be.common.exception.NotFoundRestaurantException
import com.restaurant.be.common.exception.NotFoundUserEmailException
import com.restaurant.be.common.redis.RedisRepository
import com.restaurant.be.restaurant.presentation.dto.GetRestaurantResponse
import com.restaurant.be.restaurant.presentation.dto.GetRestaurantsRequest
import com.restaurant.be.restaurant.presentation.dto.GetRestaurantsResponse
import com.restaurant.be.restaurant.repository.RestaurantEsRepository
import com.restaurant.be.restaurant.repository.RestaurantRepository
import com.restaurant.be.user.repository.UserRepository
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetRestaurantService(
    private val restaurantEsRepository: RestaurantEsRepository,
    private val redisRepository: RedisRepository,
    private val userRepository: UserRepository,
    private val restaurantRepository: RestaurantRepository
) {

    @Transactional(readOnly = true)
    fun getRestaurants(
        request: GetRestaurantsRequest,
        pageable: Pageable,
        email: String
    ): GetRestaurantsResponse {
        val user = userRepository.findByEmail(email) ?: throw NotFoundUserEmailException()

        val restaurants = restaurantEsRepository.searchRestaurants(request, pageable)

        if (!request.query.isNullOrEmpty()) {
            redisRepository.addSearchQuery(user.id ?: 0, request.query)
        }

        val restaurantProjections = restaurantRepository.findDtoByIds(
            restaurants.map { it.id },
            user.id ?: 0,
            request.like,
            pageable
        )

        return GetRestaurantsResponse(
            PageImpl(
                restaurantProjections.content.map { it.toDto() },
                pageable,
                restaurantProjections.content.size.toLong()
            )
        )
    }

    @Transactional(readOnly = true)
    fun getRestaurant(restaurantId: Long, email: String): GetRestaurantResponse {
        val restaurant = restaurantRepository.findDtoById(restaurantId)
            ?: throw NotFoundRestaurantException()
        return GetRestaurantResponse(restaurant.toDto())
    }
}

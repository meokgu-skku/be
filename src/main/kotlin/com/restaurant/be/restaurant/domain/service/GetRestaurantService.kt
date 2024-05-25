package com.restaurant.be.restaurant.domain.service

import com.restaurant.be.common.exception.NotFoundRestaurantException
import com.restaurant.be.common.exception.NotFoundUserEmailException
import com.restaurant.be.common.redis.RedisRepository
import com.restaurant.be.restaurant.presentation.controller.dto.GetRestaurantResponse
import com.restaurant.be.restaurant.presentation.controller.dto.GetRestaurantsRequest
import com.restaurant.be.restaurant.presentation.controller.dto.GetRestaurantsResponse
import com.restaurant.be.restaurant.repository.RestaurantEsRepository
import com.restaurant.be.restaurant.repository.RestaurantLikeRepository
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
    private val restaurantRepository: RestaurantRepository,
    private val restaurantLikeRepository: RestaurantLikeRepository
) {

    @Transactional(readOnly = true)
    fun getRestaurants(
        request: GetRestaurantsRequest,
        pageable: Pageable,
        email: String
    ): GetRestaurantsResponse {
        val userId = userRepository.findByEmail(email)?.id ?: throw NotFoundUserEmailException()
        val restaurantIds =
            if (request.like != null) {
                restaurantLikeRepository.findAllByUserId(userId)
                    .map { it.restaurantId }
            } else {
                null
            }

        val restaurants = restaurantEsRepository.searchRestaurants(
            request,
            pageable,
            restaurantIds,
            request.like
        )

        if (!request.query.isNullOrEmpty()) {
            redisRepository.addSearchQuery(userId, request.query)
        }

        val restaurantProjections = restaurantRepository.findDtoByIds(
            restaurants.map { it.id },
            userId
        )

        return GetRestaurantsResponse(
            PageImpl(
                restaurantProjections.map { it.toDto() },
                pageable,
                restaurantProjections.size.toLong()
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

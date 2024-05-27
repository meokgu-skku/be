package com.restaurant.be.restaurant.domain.service

import com.restaurant.be.common.exception.NotFoundRestaurantException
import com.restaurant.be.common.exception.NotFoundUserEmailException
import com.restaurant.be.common.exception.NotFoundUserException
import com.restaurant.be.restaurant.domain.entity.RestaurantLike
import com.restaurant.be.restaurant.presentation.controller.dto.GetLikeRestaurantsResponse
import com.restaurant.be.restaurant.presentation.controller.dto.LikeRestaurantResponse
import com.restaurant.be.restaurant.repository.RestaurantLikeRepository
import com.restaurant.be.restaurant.repository.RestaurantRepository
import com.restaurant.be.user.repository.UserRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LikeRestaurantService(
    private val restaurantRepository: RestaurantRepository,
    private val restaurantLikeRepository: RestaurantLikeRepository,
    private val userRepository: UserRepository
) {
    @Transactional
    fun likeRestaurant(email: String, restaurantId: Long, isLike: Boolean): LikeRestaurantResponse {
        val userId: Long = userRepository.findByEmail(email)?.id ?: throw NotFoundUserException()

        val restaurantDto = restaurantRepository.findDtoById(restaurantId)
            ?: throw NotFoundRestaurantException()

        val restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow { NotFoundRestaurantException() }

        if (isLike) {
            if (!restaurantDto.isLike) {
                restaurantLikeRepository.save(
                    RestaurantLike(
                        restaurantId = restaurantId,
                        userId = userId
                    )
                )
                restaurant.likeCount += 1
            }
        } else {
            if (restaurantDto.isLike) {
                restaurant.likeCount -= 1
                restaurantLikeRepository.deleteByUserIdAndRestaurantId(userId, restaurantId)
            }
        }

        restaurantRepository.save(restaurant)
        return LikeRestaurantResponse(restaurantRepository.findDtoById(restaurantId)!!.toDto())
    }

    @Transactional(readOnly = true)
    fun getMyLikeRestaurant(pageable: Pageable, email: String): GetLikeRestaurantsResponse {
        val userId = userRepository.findByEmail(email)?.id ?: throw NotFoundUserEmailException()

        return GetLikeRestaurantsResponse(
            restaurantRepository.findMyLikeRestaurants(userId, pageable)
                .map { it.toDto() }
        )
    }
}

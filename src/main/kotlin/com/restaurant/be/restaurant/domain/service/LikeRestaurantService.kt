package com.restaurant.be.restaurant.domain.service

import com.restaurant.be.common.exception.NotFoundRestaurantException
import com.restaurant.be.common.exception.NotFoundUserEmailException
import com.restaurant.be.common.exception.NotFoundUserException
import com.restaurant.be.restaurant.domain.entity.RestaurantLike
import com.restaurant.be.restaurant.presentation.controller.dto.GetLikeRestaurantsResponse
import com.restaurant.be.restaurant.presentation.controller.dto.LikeRestaurantResponse
import com.restaurant.be.restaurant.repository.RestaurantLikeRepository
import com.restaurant.be.restaurant.repository.RestaurantRepository
import com.restaurant.be.restaurant.repository.dto.RestaurantProjectionDto
import com.restaurant.be.user.domain.entity.User
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
        val user: User = userRepository.findByEmail(email) ?: throw NotFoundUserEmailException()
        val userId: Long = user.id ?: throw NotFoundUserException()

        val restaurant: RestaurantProjectionDto = restaurantRepository.findDtoById(restaurantId)
            ?: throw NotFoundRestaurantException()

        // 좋아요 요청
        if (isLike) {
            // 실제 좋아요가 아닐 시 Insert
            if (!restaurant.isLike) {
                restaurantLikeRepository.save(
                    RestaurantLike(
                        restaurantId = restaurantId,
                        userId = userId
                    )
                )
            }
        } else {
            restaurantLikeRepository.deleteByUserIdAndRestaurantId(userId, restaurantId)
        }

        return LikeRestaurantResponse(restaurantRepository.findDtoById(restaurantId)!!.toDto())
    }

    @Transactional
    fun getMyLikeRestaurant(pageable: Pageable, email: String): GetLikeRestaurantsResponse {
        val userId = userRepository.findByEmail(email)?.id ?: throw NotFoundUserEmailException()

        return GetLikeRestaurantsResponse(
            restaurantRepository.findMyLikeRestaurants(userId, pageable)
                .map { it.toDto() }
        )
    }
}

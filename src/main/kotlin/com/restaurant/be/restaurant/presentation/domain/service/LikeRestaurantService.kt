package com.restaurant.be.restaurant.presentation.domain.service

import com.restaurant.be.common.exception.NotFoundRestaurantException
import com.restaurant.be.common.exception.NotFoundUserEmailException
import com.restaurant.be.common.exception.NotFoundUserException
import com.restaurant.be.restaurant.presentation.domain.entity.RestaurantLike
import com.restaurant.be.restaurant.presentation.dto.GetLikeRestaurantsResponse
import com.restaurant.be.restaurant.presentation.dto.LikeRestaurantResponse
import com.restaurant.be.restaurant.presentation.dto.common.RestaurantDto
import com.restaurant.be.restaurant.repository.RestaurantLikeRepository
import com.restaurant.be.restaurant.repository.RestaurantRepository
import com.restaurant.be.restaurant.repository.dto.RestaurantProjectionDto
import com.restaurant.be.user.domain.entity.User
import com.restaurant.be.user.repository.UserRepository
import org.springframework.data.domain.Page
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
                restaurantLikeRepository.save(RestaurantLike(restaurantId = restaurantId, userId = userId))
            }
        } else {
            restaurantLikeRepository.deleteByUserIdAndRestaurantId(userId, restaurantId)
        }

        return LikeRestaurantResponse(restaurant.toDto())
    }

    @Transactional
    fun getLikeRestaurant(pageable: Pageable, email: String): GetLikeRestaurantsResponse {
        val user: User = userRepository.findByEmail(email) ?: throw NotFoundUserEmailException()
        val userId: Long = user.id ?: throw NotFoundUserException()
        // user 가 좋아요한 식당 아이디에 맞는 식당 리스트 반환
        val restaurants: Page<RestaurantDto> = restaurantLikeRepository.findRestaurantLikesByUserId(userId, pageable)

        return GetLikeRestaurantsResponse(restaurants)
    }
}

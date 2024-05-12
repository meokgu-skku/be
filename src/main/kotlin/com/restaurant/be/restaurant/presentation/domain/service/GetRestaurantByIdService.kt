package com.restaurant.be.restaurant.presentation.domain.service

import com.restaurant.be.restaurant.presentation.domain.entity.Restaurants
import com.restaurant.be.restaurant.presentation.dto.GetRestaurantResponse
import com.restaurant.be.restaurant.presentation.dto.common.RestaurantDto
import com.restaurant.be.restaurant.presentation.repository.RestaurantLikesRepository
import com.restaurant.be.restaurant.presentation.repository.RestaurantsRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetRestaurantByIdService(
    private val restaurantsRepository: RestaurantsRepository,
    private val restaurantLikesRepository: RestaurantLikesRepository
) {
    @Transactional
    fun getRestaurantById(restaurantId: Long, userId: Long): GetRestaurantResponse {
        // id가 일치하는 Restaurants Entity를 가져움
        val restaurant: Restaurants? = restaurantsRepository.findById(restaurantId)

        // Restaurants Entity를 RestaurantDto로 변환하여 GetRestaurantResponse에 저장 후 반환
        return if (restaurant != null) {
            var restaurantDto = restaurant.toDto()
            // restaurant_likes(RestaurantLikes Entity)테이블에서 유저 좋아요 여부 조회
            val restaurantLike = restaurantLikesRepository.findByUserIdAndRestaurantIdAndLikeTrue(userId, restaurantId)
            if (restaurantLike != null) {
                restaurantDto.isLike = restaurantLike.like
            }

            GetRestaurantResponse(restaurantDto)
        } else {
            GetRestaurantResponse(RestaurantDto())
        }
    }
}

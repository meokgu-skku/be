package com.restaurant.be.restaurant.presentation.domain.service

import com.restaurant.be.restaurant.presentation.domain.entity.Restaurants
import com.restaurant.be.restaurant.presentation.dto.GetRestaurantResponse
import com.restaurant.be.restaurant.presentation.repository.RestaurantLikeRepository
import com.restaurant.be.restaurant.presentation.repository.RestaurantRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetRestaurantByIdService(
    private val restaurantRepository: RestaurantRepository,
    private val restaurantLikeRepository: RestaurantLikeRepository
) {
    @Transactional
    fun getRestaurantById(restaurantId: Long, userName: String): GetRestaurantResponse {
        // id가 일치하는 Restaurants Entity 를 가져움
        val restaurant: Restaurants? = restaurantRepository.findById(restaurantId)

        // Restaurants Entity 를 RestaurantDto 로 변환하여 GetRestaurantResponse 에 저장 후 반환
        if (restaurant != null) {
            // restaurant_likes(RestaurantLikes Entity)테이블에서 유저 좋아요 여부 조회
            val isLike = restaurantLikeRepository
                .findByUserNameAndRestaurantId(userName, restaurantId)?.let { true } ?: false

            val restaurantDto = restaurant.toDto(isLike)
            return GetRestaurantResponse(restaurantDto)
        } else {
            throw Exception("레스토랑 정보가 존재하지 않습니다.")
        }
    }
}

package com.restaurant.be.restaurant.presentation.domain.service

import com.restaurant.be.common.exception.NotFoundRestaurantException
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
    fun getRestaurantById(restaurantId: Long, email: String): GetRestaurantResponse {
        // id가 일치하는 Restaurants Entity 를 가져움. 없으면 return status 404
        val restaurant: Restaurants = restaurantRepository.findById(restaurantId) ?: throw NotFoundRestaurantException()

        // Restaurants Entity 를 RestaurantDto 로 변환하여 GetRestaurantResponse 에 저장 후 반환
        // restaurant_likes(RestaurantLikes Entity)테이블에서 유저 좋아요 여부 조회
        val isLike = restaurantLikeRepository
            .findByEmailAndRestaurantId(email, restaurantId)?.let { true } ?: false
        val restaurantDto = restaurant.toDto(isLike)
        return GetRestaurantResponse(restaurantDto)

    }
}

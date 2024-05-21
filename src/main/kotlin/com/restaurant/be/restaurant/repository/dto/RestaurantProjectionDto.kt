package com.restaurant.be.restaurant.repository.dto

import com.restaurant.be.restaurant.presentation.domain.entity.Category
import com.restaurant.be.restaurant.presentation.domain.entity.Menu
import com.restaurant.be.restaurant.presentation.domain.entity.Restaurant
import com.restaurant.be.restaurant.presentation.dto.common.RestaurantDetailDto
import com.restaurant.be.restaurant.presentation.dto.common.RestaurantDto
import com.restaurant.be.review.domain.entity.Review
import kotlinx.serialization.json.JsonNull.content

data class RestaurantProjectionDto(
    val restaurant: Restaurant,
    val isLike: Boolean,
    val menus: List<Menu>,
    val review: List<Review>,
    val categories: List<Category>
) {
    fun toDto(): RestaurantDto {
        return RestaurantDto(
            id = restaurant.id,
            representativeImageUrl = restaurant.representativeImageUrl,
            name = restaurant.name,
            ratingAvg = restaurant.ratingAvg,
            reviewCount = restaurant.reviewCount,
            likeCount = restaurant.likeCount,
            categories = categories.map { it.name },
            representativeMenu = menus.firstOrNull()?.toDto(),
            operatingStartTime = "",
            operatingEndTime = "",
            representativeReviewContent = review.first().content,
            isLike = isLike,
            discountContent = restaurant.discountContent,
            detailInfo = RestaurantDetailDto(
                contactNumber = restaurant.contactNumber,
                address = restaurant.address,
                menus = menus.map { it.toDto() },
                operatingInfos = emptyList()
            )
        )
    }
}

package com.restaurant.be.restaurant.repository.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RestaurantEsDocument(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("original_category") val originalCategory: String,
    @SerialName("naver_review_count") val naverReviewCount: Long,
    @SerialName("address") val address: String,
    @SerialName("naver_rating_avg") val naverRatingAvg: Double?,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("category") val category: String,
    @SerialName("discount_content") val discountContent: String?,
    @SerialName("menus") val menus: List<MenuEsDocument>,
    @SerialName("review_count") val reviewCount: Long,
    @SerialName("rating_avg") val ratingAvg: Double?
)

@Serializable
data class MenuEsDocument(
    @SerialName("restaurant_id") val restaurantId: Int,
    @SerialName("menu_name") val menuName: String,
    @SerialName("price") val price: Int,
    @SerialName("description") val description: String,
    @SerialName("image_url") val imageUrl: String? = null
)

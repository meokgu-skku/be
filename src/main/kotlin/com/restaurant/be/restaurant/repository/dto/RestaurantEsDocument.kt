package com.restaurant.be.restaurant.repository.dto

import com.restaurant.be.restaurant.presentation.dto.common.MenuDto
import com.restaurant.be.restaurant.presentation.dto.common.RestaurantDetailDto
import com.restaurant.be.restaurant.presentation.dto.common.RestaurantDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.springframework.data.jpa.domain.AbstractPersistable_.id

@Serializable
data class RestaurantEsDocument(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("original_category") val originalCategory: String,
    @SerialName("naver_review_count") val naverReviewCount: Long,
    @SerialName("address") val address: String,
    @SerialName("naver_rating_avg") val naverRating: Double?,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("category") val category: String,
    @SerialName("discount_content") val discountContent: String?,
    @SerialName("menus") val menus: List<MenuEsDocument>,
    @SerialName("review_count") val reviewCount: Long,
    @SerialName("rating_avg") val ratingAvg: Double?,
) {
    fun toDto() = RestaurantDto(
        id = id,
        representativeImageUrl = imageUrl,
        name = name,
        ratingAvg = naverRating ?: 0.0, // RDB로 변경 필요
        reviewCount = naverReviewCount, // RDB로 변경 필요
        likeCount = 0, // RDB
        categories = category.split(",").map { it.trim() },
        representativeMenu = menus.firstOrNull()?.toDto(),
        operatingStartTime = "", // 나중에 추가
        operatingEndTime = "", // 나중에 추가
        representativeReviewContent = "", // RDB
        isLike = false, // RDB
        discountContent = discountContent, // 나중에 추가
        detailInfo = RestaurantDetailDto(
            contactNumber = "",
            address = address,
            menus = menus.map { it.toDto() },
            operatingInfos = emptyList() // 나중에 추가
        )
    )
}

@Serializable
data class MenuEsDocument(
    @SerialName("restaurant_id") val restaurantId: Int,
    @SerialName("menu_name") val menuName: String,
    @SerialName("price") val price: Int,
    @SerialName("description") val description: String,
    @SerialName("image_url") val imageUrl: String? = null
) {
    fun toDto() = MenuDto(
        name = menuName,
        price = price,
        description = description,
        isRepresentative = false,
        imageUrl = imageUrl
    )
}

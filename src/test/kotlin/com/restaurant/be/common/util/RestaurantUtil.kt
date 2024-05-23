package com.restaurant.be.common.util

import com.restaurant.be.restaurant.presentation.domain.entity.Menu
import com.restaurant.be.restaurant.presentation.domain.entity.Restaurant

object RestaurantUtil {

    fun generateRestaurantDocument(
        id: Long,
        name: String = "default_name",
        originalCategory: String = "default_category",
        address: String = "default_address",
        naverReviewCount: Long = 0,
        naverRatingAvg: Float = 0.0f,
        reviewCount: Long = 0,
        ratingAvg: Float = 0.0f,
        likeCount: Long = 0,
        number: String = "default_number",
        imageUrl: String = "default_image_url",
        category: String = "default_category",
        discountContent: String = "default_discount_content",
        menus: List<MenuDocument> = emptyList()
    ): RestaurantDocument {
        return RestaurantDocument(
            id = id,
            name = name,
            originalCategory = originalCategory,
            address = address,
            naverReviewCount = naverReviewCount,
            naverRatingAvg = naverRatingAvg,
            reviewCount = reviewCount,
            ratingAvg = ratingAvg,
            likeCount = likeCount,
            number = number,
            imageUrl = imageUrl,
            category = category,
            discountContent = discountContent,
            menus = menus
        )
    }

    fun generateRestaurantEntity(
        id: Long = 0,
        name: String = "default_name",
        originalCategories: String = "default_category",
        reviewCount: Long = 0,
        likeCount: Long = 0,
        address: String = "default_address",
        contactNumber: String = "default_number",
        ratingAvg: Double = 0.0,
        representativeImageUrl: String = "default_image_url",
        viewCount: Long = 0,
        discountContent: String? = null,
        menus: MutableList<Menu> = mutableListOf()
    ): Restaurant {
        return Restaurant(
            id = id,
            name = name,
            originalCategories = originalCategories,
            reviewCount = reviewCount,
            likeCount = likeCount,
            address = address,
            contactNumber = contactNumber,
            ratingAvg = ratingAvg,
            representativeImageUrl = representativeImageUrl,
            viewCount = viewCount,
            discountContent = discountContent,
            menus = menus
        )
    }
}

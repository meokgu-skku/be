package com.restaurant.be.common.util

import com.restaurant.be.restaurant.domain.entity.Menu
import com.restaurant.be.restaurant.domain.entity.Restaurant
import org.elasticsearch.common.geo.GeoPoint

object RestaurantUtil {

    fun generateRestaurantDocument(
        id: Long,
        name: String = "default_name",
        originalCategory: String = "default_category",
        address: String = "default_address",
        naverReviewCount: Long = 0,
        naverRatingAvg: Double = 0.0,
        reviewCount: Long = 0,
        ratingAvg: Double = 0.0,
        likeCount: Long = 0,
        number: String = "default_number",
        imageUrl: String = "default_image_url",
        category: String = "default_category",
        discountContent: String? = "default_discount_content",
        menus: List<MenuDocument> = emptyList(),
        latitude: Double = 0.0,
        longitude: Double = 0.0
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
            menus = menus,
            location = GeoPoint(latitude, longitude)
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
        menus: MutableList<Menu> = mutableListOf(),
        longitude: Double = 0.0,
        latitude: Double = 0.0
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
            menus = menus,
            longitude = longitude,
            latitude = latitude,
            naverRatingAvg = 0.0,
            naverReviewCount = 0
        )
    }

    fun generateMenuEntity(
        id: Long = 0,
        restaurantId: Long = 0,
        name: String = "default_name",
        price: Int = 0,
        description: String = "default_description",
        isRepresentative: Boolean = false,
        imageUrl: String = "default_image_url"
    ): Menu {
        return Menu(
            id = id,
            restaurantId = restaurantId,
            name = name,
            price = price,
            description = description,
            isRepresentative = isRepresentative,
            imageUrl = imageUrl
        )
    }

    fun generateMenuDocument(
        restaurantId: Long,
        menuName: String = "default_menu_name",
        price: Int = 0,
        description: String = "default_description",
        isRepresentative: String = "default_is_representative",
        imageUrl: String = "default_image_url"
    ): MenuDocument {
        return MenuDocument(
            restaurantId = restaurantId,
            menuName = menuName,
            price = price,
            description = description,
            isRepresentative = isRepresentative,
            imageUrl = imageUrl
        )
    }
}

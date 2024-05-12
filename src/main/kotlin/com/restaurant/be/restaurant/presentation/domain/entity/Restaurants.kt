package com.restaurant.be.restaurant.presentation.domain.entity

import com.restaurant.be.restaurant.presentation.dto.common.RestaurantDetailDto
import com.restaurant.be.restaurant.presentation.dto.common.RestaurantDto
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "restaurants")
class Restaurants(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "restaurant_id", nullable = false)
    var id: Long,

    @Column(name = "name", nullable = false, length = 64)
    var name: String,

    @Column(name = "category", nullable = true, length = 64)
    var category: String,

    @Column(name = "custom_category", nullable = true, length = 64)
    var customCategory: String,

    @Column(name = "review_count", nullable = false)
    var reviewCount: Long,

    @Column(name = "like_count", nullable = false)
    var likeCount: Long,

    @Column(name = "address", nullable = true, length = 256)
    var address: String,

    @Column(name = "contact_num", nullable = true, length = 32)
    var contactNum: String,

    @Column(name = "rating_avg", nullable = true)
    var ratingAvg: Double,

    @Column(name = "representative_image_url", nullable = true)
    var representativeImageUrl: String,

    @Column(name = "representative_menu", nullable = true)
    var representativeMenu: String,

    @Column(name = "kingo_pass", nullable = true)
    var kingoPass: Boolean

) {
    fun toDto(): RestaurantDto {
        return RestaurantDto(
            id = this.id,
            representativeImageUrl = this.representativeImageUrl,
            name = this.name,
            ratingAvg = this.ratingAvg,
            reviewCount = this.reviewCount,
            likeCount = this.likeCount,
            category = this.category,
            customCategory = this.customCategory,
            representativeMenu = this.representativeMenu,
            operatingStartTime = "",
            operatingEndTime = "",
            representativeReviewContent = "",
            isLike = false,
            isDiscountForSkku = false,
            discountContent = "",
            detailInfo = RestaurantDetailDto(
                contactNumber = this.contactNum,
                address = this.address
            )
        )
    }
    fun plusReviewCount() {
        this.reviewCount += this.reviewCount
    }
    fun minusReviewCount() {
        this.reviewCount -= this.reviewCount
    }

    fun plusLikeCount() {
        this.likeCount += this.likeCount
    }
    fun minusLikeCount() {
        this.likeCount -= this.likeCount
    }
}

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

    @Column(name = "category", length = 64)
    var category: String,

    @Column(name = "custom_category", nullable = false, length = 64)
    var customCategory: String,

    @Column(name = "review_count", nullable = false)
    var reviewCount: Long = 0,

    @Column(name = "like_count", nullable = false)
    var likeCount: Long = 0,

    @Column(name = "address", length = 256)
    var address: String,

    @Column(name = "contact_num", length = 32)
    var contactNum: String,

    @Column(name = "rating_avg")
    var ratingAvg: Double,

    @Column(name = "representative_image_url", length = 300)
    var representativeImageUrl: String,

    @Column(name = "representative_menu")
    var representativeMenu: String,

    @Column(name = "kingo_pass")
    var kingoPass: Boolean,

    @Column(name = "view_count", nullable = false)
    var viewCount: Long = 0,

    @Column(name = "menus")
    var menus: String,

    @Column(name = "operating_infos")
    var operatingInfos: String

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

    fun toDto(isLike: Boolean): RestaurantDto {
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
            isLike = isLike,
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

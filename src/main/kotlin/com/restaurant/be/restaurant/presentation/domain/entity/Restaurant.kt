package com.restaurant.be.restaurant.presentation.domain.entity

import com.restaurant.be.restaurant.presentation.dto.common.RestaurantDetailDto
import com.restaurant.be.restaurant.presentation.dto.common.RestaurantDto
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "restaurants")
class Restaurant(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    var id: Long,

    @Column(name = "name", nullable = false, length = 64)
    var name: String,

    // 메뉴 세부 카테고리 (ex. 돈가스, 치킨, 장어요리) - restaurants.csv category 열
    @Column(name = "category_detail", nullable = false, length = 64)
    var categoryDetail: String,

    @Column(name = "review_count", nullable = false)
    var reviewCount: Long = 0,

    @Column(name = "like_count", nullable = false)
    var likeCount: Long = 0,

    @Column(name = "address", length = 256)
    var address: String,

    @Column(name = "contact_number", length = 32)
    var contactNumber: String,

    @Column(name = "rating_avg")
    var ratingAvg: Double,

    @Column(name = "representative_image_url", length = 300)
    var representativeImageUrl: String,

    @Column(name = "view_count", nullable = false)
    var viewCount: Long = 0,

    @Column(name = "discount_content")
    var discountContent: Boolean,

    @OneToMany(mappedBy = "restaurantId", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    var category: MutableSet<Category> = mutableSetOf()

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
            categoryDetail = this.categoryDetail,
            operatingStartTime = "",
            operatingEndTime = "",
            representativeReviewContent = "",
            isLike = false,
            isDiscountForSkku = false,
            discountContent = "",
            detailInfo = RestaurantDetailDto(
                contactNumber = this.contactNumber,
                address = this.address,
                menus = emptyList(), // TODO: 메뉴 정보 추가
                operatingInfos = emptyList() // TODO: 영업 정보 추가
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
            categoryDetail = this.categoryDetail,
            operatingStartTime = "",
            operatingEndTime = "",
            representativeReviewContent = "",
            isLike = isLike,
            isDiscountForSkku = false,
            discountContent = "",
            detailInfo = RestaurantDetailDto(
                contactNumber = this.contactNumber,
                address = this.address,
                menus = emptyList(), // TODO: 메뉴 정보 추가
                operatingInfos = emptyList() // TODO: 영업 정보 추가
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

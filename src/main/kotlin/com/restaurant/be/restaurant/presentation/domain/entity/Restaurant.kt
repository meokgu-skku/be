package com.restaurant.be.restaurant.presentation.domain.entity

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

    @Column(name = "original_categories", nullable = false, length = 64)
    var originalCategories: String,

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
    var discountContent: String? = null,

    @OneToMany(mappedBy = "restaurantId", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var menus: MutableList<Menu> = mutableListOf()

) {
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

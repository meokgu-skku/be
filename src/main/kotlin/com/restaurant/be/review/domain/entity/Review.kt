package com.restaurant.be.review.domain.entity

import com.restaurant.be.common.entity.BaseEntity
import com.restaurant.be.review.presentation.dto.common.ReviewResponseDto
import com.restaurant.be.user.domain.entity.User
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "restaurant_reviews")
class Review(
    @Id
    @Column(name = "review_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false)
    val restaurantId: Long,

    @Column(nullable = false)
    val content: String,

    @Column(nullable = false)
    val rating: Double,

    @Column(nullable = false)
    val isLike: Boolean,

    // 부모 (Review Entity)가 주인이되어 Image참조 가능. 반대는 불가능
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "review_id")
    val images: MutableList<ReviewImage> = mutableListOf()

) : BaseEntity() {
    fun addImage(reviewImage: ReviewImage) {
        images.add(reviewImage)
    }

    fun toResponseDTO(): ReviewResponseDto {
        return ReviewResponseDto(
            userId = user.id,
            restaurantId = restaurantId,
            rating = rating,
            comment = content,
            imageUrls = images.map { it.imageUrl },
            isLike = isLike
        )
    }
}

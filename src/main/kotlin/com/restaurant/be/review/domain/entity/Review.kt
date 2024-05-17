package com.restaurant.be.review.domain.entity

import com.restaurant.be.common.entity.BaseEntity
import com.restaurant.be.review.domain.entity.QReview.review
import com.restaurant.be.review.presentation.dto.common.ReviewRequestDto
import com.restaurant.be.review.presentation.dto.common.ReviewResponseDto
import com.restaurant.be.user.domain.entity.QUser.user
import com.restaurant.be.user.domain.entity.User
import kotlinx.serialization.json.JsonNull.content
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false)
    val restaurantId: Long,

    @Column(nullable = false)
    var content: String,

    @Column(nullable = false)
    var rating: Double,

    @Column(name = "like_count", nullable = false)
    val likeCount: Long = 0,

    @Column(name = "view_count", nullable = false)
    val viewCount: Long = 0,

    // 부모 (Review Entity)가 주인이되어 Image참조 가능. 반대는 불가능
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "review_id")
    var images: MutableList<ReviewImage> = mutableListOf()

) : BaseEntity() {
    fun addImage(reviewImage: ReviewImage) {
        images.add(reviewImage)
    }

    fun updateReview(request: ReviewRequestDto) {
        this.content = request.content
        this.rating = request.rating
        this.images.clear()
        request.imageUrls.forEach {
            this.addImage(
                ReviewImage(
                    imageUrl = it
                )
            )
        }
    }

    fun toResponseDTO(doesUserLike: Boolean): ReviewResponseDto {
        return ReviewResponseDto(
            id = id,
            userId = user.id ?: 0,
            username = user.nickname,
            profileImageUrl = user.profileImageUrl,
            restaurantId = restaurantId,
            rating = rating,
            content = content,
            imageUrls = images.map { it.imageUrl },
            isLike = doesUserLike,
            viewCount = viewCount,
            likeCount = likeCount
        )
    }
}

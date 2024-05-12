package com.restaurant.be.review.domain.entity

import com.restaurant.be.common.entity.BaseEntity
import com.restaurant.be.user.domain.entity.User
import javax.persistence.*

@Entity
@Table(name = "restaurant_reviews")
class Review(
    @Id
    @Column(name = "review_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long? = null,

    @ManyToOne()
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = true)
    val restaurantId: Long? = null,

    @Column(nullable = false)
    val content: String,
    @Column(nullable = false)
    val rating: Int,

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
}

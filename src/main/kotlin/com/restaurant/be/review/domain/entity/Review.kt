package com.restaurant.be.review.domain.entity

import com.restaurant.be.user.domain.entity.User
import javax.persistence.*

@Entity
@Table(name = "restaurant_reviews")
class Review(
    @Id
    @Column(name = "review_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column(nullable = true)
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "restaurant_id", nullable = false)
    var restaurantId: Long? = null,

    @Column(nullable = false)
    var reviewContent: String
)
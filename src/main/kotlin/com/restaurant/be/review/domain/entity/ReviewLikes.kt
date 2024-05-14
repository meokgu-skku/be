
package com.restaurant.be.review.domain.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.Table

@Entity
@Table(name = "review_likes")
class ReviewLikes(
    @Id
    @Column(name = "review_likes_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long? = null,

    @JoinColumn(name = "user_id", nullable = false)
    val userId: Long,

    @JoinColumn(name = "review_id", nullable = false)
    val reviewId: Long
)

package com.restaurant.be.review.domain.entity

import javax.persistence.*

@Entity
@Table(name = "review_images")
class ReviewImage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val imageUrl: String
)

package com.restaurant.be.restaurant.presentation.domain.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "restaurant_likes")
class RestaurantLike(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    var id: Long,

    @Column(name = "restaurant_id", nullable = false)
    var restaurantId: Long,

    @Column(name = "user_id", nullable = false)
    var userId: Long
)

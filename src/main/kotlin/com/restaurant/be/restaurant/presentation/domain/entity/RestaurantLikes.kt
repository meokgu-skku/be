package com.restaurant.be.restaurant.presentation.domain.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "restaurant_likes")
class RestaurantLikes(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    var id: Long,

    @Column(name = "restaurant_id", nullable = false)
    var restaurantId: Long,

    @Column(name = "email", nullable = false)
    var email: String  // user의 email 정보

)
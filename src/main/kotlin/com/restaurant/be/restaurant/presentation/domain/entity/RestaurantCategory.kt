package com.restaurant.be.restaurant.presentation.domain.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "restaurant_categories")
data class RestaurantCategory(
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null,

    @Column(name = "restaurant_id", nullable = false)
    var restaurantId: Long,

    @Column(name = "category_id", nullable = false)
    var categoryId: Long
)

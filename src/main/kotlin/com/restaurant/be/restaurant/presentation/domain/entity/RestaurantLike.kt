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
    var id: Long? = null,

    @Column(name = "restaurant_id", nullable = false)
    var restaurantId: Long,

    @Column(name = "user_id", nullable = false)
    var userId: Long
) {
    // 세컨더리 생성자 추가 id가 null이어도 db엔 자동 숫자 생성됨
    constructor(restaurantId: Long, email: Long) : this(null, restaurantId, email)
}

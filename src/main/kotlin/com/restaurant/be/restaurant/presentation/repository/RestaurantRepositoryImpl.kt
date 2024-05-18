package com.restaurant.be.restaurant.presentation.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.restaurant.be.restaurant.presentation.domain.entity.QRestaurant
import com.restaurant.be.restaurant.presentation.domain.entity.Restaurant
import org.springframework.stereotype.Repository

@Repository
class RestaurantRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : RestaurantRepository {
    override fun findById(id: Long): Restaurant? {
        val restaurant = QRestaurant.restaurant
        return queryFactory.selectFrom(restaurant)
            .where(restaurant.id.eq(id))
            .fetchOne()
    }
}

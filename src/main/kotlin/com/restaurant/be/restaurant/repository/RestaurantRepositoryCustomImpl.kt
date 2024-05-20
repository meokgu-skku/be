package com.restaurant.be.restaurant.repository

import com.querydsl.core.group.GroupBy
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import com.restaurant.be.restaurant.presentation.domain.entity.QCategory.category
import com.restaurant.be.restaurant.presentation.domain.entity.QMenu.menu
import com.restaurant.be.restaurant.presentation.domain.entity.QRestaurant.restaurant
import com.restaurant.be.restaurant.presentation.domain.entity.QRestaurantCategory.restaurantCategory
import com.restaurant.be.restaurant.presentation.domain.entity.QRestaurantLike.restaurantLike
import com.restaurant.be.restaurant.repository.dto.RestaurantProjectionDto
import com.restaurant.be.review.domain.entity.QReview.review
import com.restaurant.be.user.domain.entity.QUser.user

class RestaurantRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory
) : RestaurantRepositoryCustom {
    override fun findDtoById(restaurantId: Long): RestaurantProjectionDto? {
        val result = queryFactory
            .from(restaurant)
            .leftJoin(restaurantLike).on(restaurantLike.restaurantId.eq(restaurant.id))
            .leftJoin(user).on(restaurantLike.userId.eq(user.id))
            .leftJoin(menu).on(menu.restaurantId.eq(restaurant.id))
            .leftJoin(restaurantCategory).on(restaurantCategory.restaurantId.eq(restaurant.id))
            .leftJoin(category).on(restaurantCategory.categoryId.eq(category.id))
            .leftJoin(review).on(review.restaurantId.eq(restaurant.id))
            .where(restaurant.id.eq(restaurantId))
            .transform(
                GroupBy.groupBy(restaurant.id).list(
                    Projections.constructor(
                        RestaurantProjectionDto::class.java,
                        restaurant,
                        restaurantLike.userId.isNotNull(),
                        GroupBy.list(menu),
                        GroupBy.list(review),
                        GroupBy.list(category)
                    )
                )
            )

        return result.firstOrNull()
    }
}

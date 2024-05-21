package com.restaurant.be.restaurant.repository

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
        val restaurantInfo = queryFactory
            .select(restaurant)
            .from(restaurant)
            .where(restaurant.id.eq(restaurantId))
            .fetchOne()

        val likedUsers = queryFactory
            .select(user.id)
            .from(restaurantLike)
            .leftJoin(user).on(restaurantLike.userId.eq(user.id))
            .where(restaurantLike.restaurantId.eq(restaurantId))
            .fetch()

        val menus = queryFactory
            .select(menu)
            .from(menu)
            .where(menu.restaurantId.eq(restaurantId))
            .fetch()

        val review = queryFactory
            .select(review)
            .from(review)
            .where(review.restaurantId.eq(restaurantId))
            .orderBy(review.likeCount.desc())
            .limit(1)
            .fetchOne()

        val categories = queryFactory
            .select(category)
            .from(restaurantCategory)
            .leftJoin(category).on(restaurantCategory.categoryId.eq(category.id))
            .where(restaurantCategory.restaurantId.eq(restaurantId))
            .fetch()

        return if (restaurantInfo != null) {
            RestaurantProjectionDto(
                restaurantInfo,
                likedUsers.isNotEmpty(),
                menus,
                review,
                categories
            )
        } else {
            null
        }
    }
}

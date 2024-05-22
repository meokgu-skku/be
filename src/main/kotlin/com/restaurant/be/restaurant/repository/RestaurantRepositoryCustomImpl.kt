package com.restaurant.be.restaurant.repository

import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import com.restaurant.be.restaurant.presentation.domain.entity.QCategory.category
import com.restaurant.be.restaurant.presentation.domain.entity.QMenu.menu
import com.restaurant.be.restaurant.presentation.domain.entity.QRestaurant.restaurant
import com.restaurant.be.restaurant.presentation.domain.entity.QRestaurantCategory.restaurantCategory
import com.restaurant.be.restaurant.presentation.domain.entity.QRestaurantLike.restaurantLike
import com.restaurant.be.restaurant.repository.dto.RestaurantProjectionDto
import com.restaurant.be.review.domain.entity.QReview.review
import com.restaurant.be.user.domain.entity.QUser.user
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

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

    override fun findDtoByIds(
        restaurantIds: List<Long>,
        userId: Long,
        isLikeFilter: Boolean?,
        pageable: Pageable
    ): Page<RestaurantProjectionDto> {
        val restaurantQuery = queryFactory
            .select(restaurant)
            .from(restaurant)
            .where(
                restaurant.id.`in`(restaurantIds)
                    .and(
                        if (isLikeFilter == true) {
                            restaurant.id.`in`(
                                JPAExpressions
                                    .select(restaurantLike.restaurantId)
                                    .from(restaurantLike)
                                    .where(restaurantLike.userId.eq(userId))
                            )
                        } else {
                            null
                        }
                    )
            )

        val total = restaurantQuery.fetchCount()

        val restaurantInfos = restaurantQuery
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val likedUsers = queryFactory
            .select(restaurantLike)
            .from(restaurantLike)
            .where(restaurantLike.userId.eq(userId))
            .fetch()

        val menus = queryFactory
            .select(menu)
            .from(menu)
            .where(menu.restaurantId.`in`(restaurantIds))
            .fetch()

        val reviews = queryFactory
            .select(review)
            .from(review)
            .where(review.restaurantId.`in`(restaurantIds))
            .orderBy(review.likeCount.desc())
            .fetch()

        val categories = queryFactory
            .select(restaurantCategory, category)
            .from(restaurantCategory)
            .leftJoin(category).on(restaurantCategory.categoryId.eq(category.id))
            .where(restaurantCategory.restaurantId.`in`(restaurantIds))
            .fetch()

        val restaurantDtos = restaurantInfos.map { restaurantInfo ->
            val likedUserIds =
                likedUsers.filter { it.restaurantId == restaurantInfo.id }.map { it.id }
            val menuList = menus.filter { it.restaurantId == restaurantInfo.id }
            val review = reviews.firstOrNull { it.restaurantId == restaurantInfo.id }
            val categoryList = categories
                .filter { it.get(restaurantCategory.restaurantId) == restaurantInfo.id }
                .mapNotNull { it.get(category) }
            RestaurantProjectionDto(
                restaurantInfo,
                likedUserIds.isNotEmpty(),
                menuList,
                review,
                categoryList
            )
        }

        return PageImpl(restaurantDtos, pageable, total)
    }
}

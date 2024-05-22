package com.restaurant.be.restaurant.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.restaurant.be.common.exception.NotFoundRestaurantException
import com.restaurant.be.restaurant.presentation.domain.entity.QRestaurantLike.restaurantLike
import com.restaurant.be.restaurant.presentation.dto.common.RestaurantDto
import org.springframework.data.domain.Pageable

class RestaurantLikeRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory,
    private val restaurantRepository: RestaurantRepository
) : RestaurantLikeRepositoryCustom {
    override fun findRestaurantLikesByUserId(
        userId: Long?,
        pageable: Pageable
    ): List<RestaurantDto> {
        println(pageable.offset)
        println(pageable.pageSize)
        val restaurantIds = queryFactory
            .select(restaurantLike.restaurantId)
            .from(restaurantLike)
            .where(restaurantLike.userId.eq(userId))
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        return restaurantIds.map { restaurantRepository.findDtoById(it)?.toDto() ?: throw NotFoundRestaurantException() }
    }
}

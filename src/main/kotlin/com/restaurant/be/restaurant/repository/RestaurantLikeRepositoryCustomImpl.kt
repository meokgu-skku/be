package com.restaurant.be.restaurant.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.restaurant.be.common.exception.NotFoundRestaurantException
import com.restaurant.be.restaurant.domain.entity.QRestaurantLike.restaurantLike
import com.restaurant.be.restaurant.presentation.controller.dto.common.RestaurantDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

class RestaurantLikeRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory,
    private val restaurantRepository: RestaurantRepository
) : RestaurantLikeRepositoryCustom {
    override fun findRestaurantLikesByUserId(
        userId: Long,
        pageable: Pageable
    ): Page<RestaurantDto> {
        val restaurantIds = queryFactory
            .select(restaurantLike.restaurantId)
            .from(restaurantLike)
            .where(restaurantLike.userId.eq(userId))
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val restaurantDtos: List<RestaurantDto> = restaurantIds.map {
            restaurantRepository.findDtoById(it)?.toDto() ?: throw NotFoundRestaurantException()
        }
        return PageImpl(restaurantDtos, pageable, restaurantDtos.size.toLong())
    }
}

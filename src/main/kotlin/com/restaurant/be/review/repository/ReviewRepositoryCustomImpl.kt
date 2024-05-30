package com.restaurant.be.review.repository

import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.PathBuilder
import com.querydsl.core.types.dsl.PathBuilderFactory
import com.querydsl.jpa.impl.JPAQueryFactory
import com.restaurant.be.review.domain.entity.QReview.review
import com.restaurant.be.review.domain.entity.QReviewLike.reviewLike
import com.restaurant.be.review.domain.entity.Review
import com.restaurant.be.review.presentation.dto.ReviewWithLikesDto
import com.restaurant.be.user.domain.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

class ReviewRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory
) : ReviewRepositoryCustom {
    override fun findReview(
        user: User,
        reviewId: Long
    ): ReviewWithLikesDto? {
        return queryFactory
            .select(
                Projections.constructor(
                    ReviewWithLikesDto::class.java,
                    review,
                    reviewLike.userId.isNotNull()
                )
            )
            .from(review)
            .leftJoin(reviewLike)
            .on(
                reviewLike.reviewId.eq(review.id)
                    .and(reviewLike.userId.eq(user.id))
            )
            .where(review.id.eq(reviewId))
            .fetchOne()
    }

    override fun findReviews(user: User, restaurantId: Long, pageable: Pageable): Page<ReviewWithLikesDto> {
        val orderSpecifier = if (!pageable.sort.isEmpty) {
            setOrderSpecifier(pageable)
        } else {
            val reviewPath = PathBuilderFactory().create(Review::class.java)
            listOf(reviewPath.getNumber("id", Long::class.java).desc())
        }

        val reviewsWithLikes = queryFactory
            .select(
                Projections.constructor(
                    ReviewWithLikesDto::class.java,
                    review,
                    reviewLike.userId.isNotNull()
                )
            )
            .from(review)
            .leftJoin(reviewLike)
            .on(
                reviewLike.reviewId.eq(review.id)
                    .and(reviewLike.userId.eq(user.id))
            )
            .where(review.restaurantId.eq(restaurantId))
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(*orderSpecifier.toTypedArray())
            .fetchJoin()
            .fetch()

        return PageImpl(
            reviewsWithLikes,
            pageable,
            reviewsWithLikes.size.toLong()
        )
    }

    override fun findMyReviews(user: User, pageable: Pageable): Page<ReviewWithLikesDto> {
        val orderSpecifier = if (!pageable.sort.isEmpty) {
            setOrderSpecifier(pageable)
        } else {
            val reviewPath = PathBuilderFactory().create(Review::class.java)
            listOf(reviewPath.getNumber("id", Long::class.java).desc())
        }

        val reviewsWithLikes = queryFactory
            .select(
                Projections.constructor(
                    ReviewWithLikesDto::class.java,
                    review,
                    reviewLike.userId.isNotNull()
                )
            )
            .from(review)
            .leftJoin(reviewLike)
            .on(
                reviewLike.reviewId.eq(review.id)
                    .and(reviewLike.userId.eq(user.id))
            )
            .where(review.user.id.eq(user.id))
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(*orderSpecifier.toTypedArray())
            .fetchJoin()
            .fetch()

        return PageImpl(
            reviewsWithLikes,
            pageable,
            reviewsWithLikes.size.toLong()
        )
    }

    private fun setOrderSpecifier(pageable: Pageable): List<OrderSpecifier<*>> {
        val pathBuilder: PathBuilder<Review> = PathBuilderFactory().create(Review::class.java)
        val sort = pageable.sort

        val orderSpecifiers: MutableList<OrderSpecifier<*>> = mutableListOf()

        for (order in sort) {
            val property = order.property
            val direction = order.direction

            val orderSpecifier: OrderSpecifier<*> = if (direction.isAscending) {
                pathBuilder.getString(property).asc()
            } else {
                pathBuilder.getString(property).desc()
            }

            orderSpecifiers.add(orderSpecifier)
        }
        return orderSpecifiers
    }
}

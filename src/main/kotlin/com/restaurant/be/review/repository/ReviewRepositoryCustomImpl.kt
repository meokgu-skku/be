package com.restaurant.be.review.repository

import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.PathBuilder
import com.querydsl.core.types.dsl.PathBuilderFactory
import com.querydsl.jpa.impl.JPAQueryFactory
import com.restaurant.be.review.domain.entity.QReview.review
import com.restaurant.be.review.domain.entity.QReviewLikes.reviewLikes
import com.restaurant.be.review.domain.entity.Review
import com.restaurant.be.review.presentation.dto.ReviewWithLikesDto
import com.restaurant.be.user.domain.entity.User
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
                    reviewLikes.userId.isNotNull()
                )
            )
            .from(review)
            .leftJoin(reviewLikes)
            .on(
                reviewLikes.reviewId.eq(review.id)
                    .and(reviewLikes.userId.eq(user.id))
            )
            .where(review.id.eq(reviewId))
            .fetchJoin()
            .fetchOne()
    }

    override fun findReviews(user: User, pageable: Pageable): List<ReviewWithLikesDto> {
        val orderSpecifier = setOrderSpecifier(pageable)

        val reviewsWithLikes = queryFactory
            .select(
                Projections.constructor(
                    ReviewWithLikesDto::class.java,
                    review,
                    reviewLikes.userId.isNotNull()
                )
            )
            .from(review)
            .leftJoin(reviewLikes)
            .on(
                reviewLikes.reviewId.eq(review.id)
                    .and(reviewLikes.userId.eq(user.id))
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(*orderSpecifier.toTypedArray())
            .fetchJoin()
            .fetch()

        return reviewsWithLikes
    }

    override fun findMyReviews(user: User, pageable: Pageable): List<ReviewWithLikesDto> {
        val orderSpecifier = setOrderSpecifier(pageable)

        val reviewsWithLikes = queryFactory
            .select(
                Projections.constructor(
                    ReviewWithLikesDto::class.java,
                    review,
                    reviewLikes.userId.isNotNull()
                )
            )
            .from(review)
            .leftJoin(reviewLikes)
            .on(
                reviewLikes.reviewId.eq(review.id)
                    .and(reviewLikes.userId.eq(user.id))
            )
            .where(review.user.id.eq(user.id))
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(*orderSpecifier.toTypedArray())
            .fetchJoin()
            .fetch()

        return reviewsWithLikes
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
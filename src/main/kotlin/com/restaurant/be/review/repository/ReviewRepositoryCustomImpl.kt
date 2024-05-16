package com.restaurant.be.review.repository

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import com.restaurant.be.review.domain.entity.QReview.review
import com.restaurant.be.review.domain.entity.QReviewLikes.reviewLikes
import com.restaurant.be.review.presentation.dto.ReviewWithLikesDto
import com.restaurant.be.user.domain.entity.User

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
}

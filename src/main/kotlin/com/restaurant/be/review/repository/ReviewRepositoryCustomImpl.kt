package com.restaurant.be.review.repository

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import com.restaurant.be.review.domain.entity.QReview
import com.restaurant.be.review.domain.entity.QReview.review
import com.restaurant.be.review.domain.entity.QReviewLikes
import com.restaurant.be.review.domain.entity.Review
import com.restaurant.be.review.presentation.dto.ReviewWithLikesDto
import com.restaurant.be.user.domain.entity.QUser.user
import com.restaurant.be.user.domain.entity.User

class ReviewRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory
) : ReviewRepositoryCustom {
    override fun findReview(
        user: User,
        review: Review
    ): ReviewWithLikesDto? {
        return queryFactory
            .select(
                Projections.constructor(
                    ReviewWithLikesDto::class.java,
                    QReview.review,
                    QReviewLikes.reviewLikes.userId.isNotNull()
                )
            )
            .from(QReview.review)
            .leftJoin(QReviewLikes.reviewLikes)
            .on(
                QReviewLikes.reviewLikes.reviewId.eq(QReview.review.id)
                    .and(QReviewLikes.reviewLikes.userId.eq(user.id))
            )
            .where(QReview.review.id.eq(review.id))
            .fetchOne()
    }
}

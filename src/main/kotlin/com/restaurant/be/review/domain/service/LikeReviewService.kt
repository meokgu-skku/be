package com.restaurant.be.review.domain.service

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import com.restaurant.be.common.exception.DuplicateLikeException
import com.restaurant.be.common.exception.NotFoundLikeException
import com.restaurant.be.common.exception.NotFoundReviewException
import com.restaurant.be.common.exception.NotFoundUserEmailException
import com.restaurant.be.review.domain.entity.QReview
import com.restaurant.be.review.domain.entity.QReviewLikes
import com.restaurant.be.review.presentation.dto.LikeReviewRequest
import com.restaurant.be.review.presentation.dto.LikeReviewResponse
import com.restaurant.be.review.presentation.dto.ReviewWithLikesDto
import com.restaurant.be.review.presentation.dto.common.ReviewResponseDto
import com.restaurant.be.review.repository.ReviewLikesRepository
import com.restaurant.be.user.domain.entity.User
import com.restaurant.be.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LikeReviewService(
    val userRepository: UserRepository,
    val reviewLikesRepository: ReviewLikesRepository,
    val jpaQueryFactory: JPAQueryFactory
) {
    @Transactional
    fun likeReview(reviewId: Long, request: LikeReviewRequest, email: String): LikeReviewResponse {
        val user = userRepository.findByEmail(email) ?: throw NotFoundUserEmailException()

        if (request.isLike) {
            if (isAlreadyLike(reviewId, user)) {
                throw DuplicateLikeException()
            }
            reviewLikesRepository.save(request.toEntity(user.id!!, reviewId))
        } else {
            if (!isAlreadyLike(reviewId, user)) {
                throw NotFoundLikeException()
            }
            reviewLikesRepository.deleteByReviewIdAndUserId(reviewId, user.id!!)
        }

        val reviewWithLikes: ReviewWithLikesDto? = joinQuery(user, reviewId)
            ?: throw NotFoundReviewException()

        val responseDto = ReviewResponseDto.toDto(
            reviewWithLikes!!.review,
            reviewWithLikes.isLikedByUser
        )

        return LikeReviewResponse(responseDto)
    }

    private fun isAlreadyLike(reviewId: Long, user: User) =
        reviewLikesRepository.existsByReviewIdAndUserId(reviewId, user.id)

    private fun joinQuery(
        user: User,
        reviewId: Long?
    ): ReviewWithLikesDto? {
        val reviewWithLikes: ReviewWithLikesDto? = jpaQueryFactory
            .from(QReview.review)
            .leftJoin(QReviewLikes.reviewLikes)
            .on(
                QReviewLikes.reviewLikes.reviewId.eq(QReview.review.id)
                    .and(QReviewLikes.reviewLikes.userId.eq(user.id))
            )
            .where(QReview.review.id.eq(reviewId))
            .select(
                Projections.constructor(
                    ReviewWithLikesDto::class.java,
                    QReview.review,
                    QReviewLikes.reviewLikes.userId.isNotNull()
                )
            )
            .fetchOne()

        return reviewWithLikes
    }
}

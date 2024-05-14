package com.restaurant.be.review.domain.service

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import com.restaurant.be.common.exception.NotFoundUserEmailException
import com.restaurant.be.review.domain.entity.QReview
import com.restaurant.be.review.domain.entity.QReview.review
import com.restaurant.be.review.domain.entity.QReviewLikes
import com.restaurant.be.review.domain.entity.Review
import com.restaurant.be.review.domain.entity.ReviewImage
import com.restaurant.be.review.presentation.dto.CreateReviewResponse
import com.restaurant.be.review.presentation.dto.ReviewWithLikesDto
import com.restaurant.be.review.presentation.dto.common.ReviewRequestDto
import com.restaurant.be.review.presentation.dto.common.ReviewResponseDto
import com.restaurant.be.review.repository.ReviewRepository
import com.restaurant.be.user.domain.entity.User
import com.restaurant.be.user.repository.UserRepository
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class CreateReviewService(
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository,
    private val jpaQueryFactory: JPAQueryFactory
) {
    @Transactional
    fun createReview(restaurantId: Long, reviewRequest: ReviewRequestDto, email: String): CreateReviewResponse {
        val user = userRepository.findByEmail(email)
            ?: throw NotFoundUserEmailException()

        val review = reviewRequest.toEntity(user, restaurantId)

        reviewRequest.imageUrls.forEach {
            review.addImage(
                ReviewImage(
                    imageUrl = it
                )
            )
        }

        reviewRepository.saveAndFlush(review)

        val reviewWithLikes: ReviewWithLikesDto? = joinQuery(user, review)

        val responseDto = ReviewResponseDto.toDto(
            reviewWithLikes!!.review,
            reviewWithLikes.isLikedByUser
        )

        return CreateReviewResponse(responseDto)
    }

    private fun joinQuery(
        user: User,
        review: Review
    ): ReviewWithLikesDto? {
        val reviewWithLikes: ReviewWithLikesDto? = jpaQueryFactory
            .from(QReview.review)
            .leftJoin(QReviewLikes.reviewLikes)
            .on(
                QReviewLikes.reviewLikes.reviewId.eq(QReview.review.id)
                    .and(QReviewLikes.reviewLikes.userId.eq(user.id))
            )
            .where(QReview.review.id.eq(review.id))
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

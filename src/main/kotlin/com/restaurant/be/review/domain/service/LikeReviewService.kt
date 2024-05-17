package com.restaurant.be.review.domain.service

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import com.restaurant.be.common.exception.DuplicateLikeException
import com.restaurant.be.common.exception.NotFoundLikeException
import com.restaurant.be.common.exception.NotFoundReviewException
import com.restaurant.be.common.exception.NotFoundUserEmailException
import com.restaurant.be.common.exception.NotFoundUserIdException
import com.restaurant.be.review.domain.entity.QReview
import com.restaurant.be.review.domain.entity.QReview.review
import com.restaurant.be.review.domain.entity.QReviewLikes
import com.restaurant.be.review.presentation.dto.LikeReviewRequest
import com.restaurant.be.review.presentation.dto.LikeReviewResponse
import com.restaurant.be.review.presentation.dto.ReviewWithLikesDto
import com.restaurant.be.review.presentation.dto.common.ReviewResponseDto
import com.restaurant.be.review.repository.ReviewLikesRepository
import com.restaurant.be.review.repository.ReviewRepository
import com.restaurant.be.user.domain.entity.User
import com.restaurant.be.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LikeReviewService(
    val userRepository: UserRepository,
    val reviewLikesRepository: ReviewLikesRepository,
    val reviewRepository: ReviewRepository,
) {
    @Transactional
    fun likeReview(reviewId: Long, request: LikeReviewRequest, email: String): LikeReviewResponse {
        val user = userRepository.findByEmail(email)
            ?: throw NotFoundUserEmailException()

        val userId = user.id ?: throw NotFoundUserIdException()

        likeReviewWhetherAlreadyLikeOrNot(request, reviewId, user, userId)

        val reviewWithLikes: ReviewWithLikesDto? = reviewRepository.findReview(user,reviewId)
            ?: throw NotFoundReviewException()

        val responseDto = ReviewResponseDto.toDto(
            reviewWithLikes!!.review,
            reviewWithLikes.isLikedByUser
        )

        return LikeReviewResponse(responseDto)
    }

    private fun likeReviewWhetherAlreadyLikeOrNot(
        request: LikeReviewRequest,
        reviewId: Long,
        user: User,
        userId: Long
    ) {
        if (request.isLike) {
            if (isAlreadyLike(reviewId, user)) {
                throw DuplicateLikeException()
            }
            reviewLikesRepository.save(request.toEntity(userId, reviewId))
            val review = reviewRepository.findById(reviewId)
            review.get().incrementLikeCount()

        } else {
            if (!isAlreadyLike(reviewId, user)) {
                throw NotFoundLikeException()
            }
            reviewLikesRepository.deleteByReviewIdAndUserId(reviewId, userId)
            val review = reviewRepository.findById(reviewId)
            review.get().decrementLikeCount()
        }
    }

    private fun isAlreadyLike(reviewId: Long, user: User) =
        reviewLikesRepository.existsByReviewIdAndUserId(reviewId, user.id)

}

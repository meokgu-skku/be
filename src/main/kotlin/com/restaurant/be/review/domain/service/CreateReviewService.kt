package com.restaurant.be.review.domain.service

import com.restaurant.be.common.exception.NotFoundUserEmailException
import com.restaurant.be.review.domain.entity.ReviewImage
import com.restaurant.be.review.presentation.dto.CreateReviewResponse
import com.restaurant.be.review.presentation.dto.common.ReviewRequestDto
import com.restaurant.be.review.repository.ReviewLikesRepository
import com.restaurant.be.review.repository.ReviewRepository
import com.restaurant.be.user.repository.UserRepository
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class CreateReviewService(
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository,
    private val reviewLikesRepository: ReviewLikesRepository
) {
    @Transactional
    fun createReviewOf(restaurantId: Long, reviewRequest: ReviewRequestDto, email: String): CreateReviewResponse {
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

        val savedReview = reviewRepository.save(review)

        return CreateReviewResponse(
            savedReview.toResponseDTO(isReviewLikedByUser(user.id, savedReview.id))
        )
    }

    fun isReviewLikedByUser(userId: Long?, reviewId: Long?): Boolean {
        return reviewLikesRepository.existsByReviewIdAndUserId(userId, reviewId)
    }
}

package com.restaurant.be.review.domain.service

import com.restaurant.be.common.exception.NotFoundUserEmailException
import com.restaurant.be.review.domain.entity.Review
import com.restaurant.be.review.domain.entity.ReviewImage
import com.restaurant.be.review.presentation.dto.CreateReviewResponse
import com.restaurant.be.review.presentation.dto.common.ReviewRequestDto
import com.restaurant.be.review.repository.ReviewRepository
import com.restaurant.be.user.repository.UserRepository
import javax.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class CreateReviewService(
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository
) {
    @Transactional
    fun createReviewOf(restaurantId: Long, reviewRequest: ReviewRequestDto, email: String): CreateReviewResponse {
        val user = userRepository.findByEmail(email)
            ?: throw NotFoundUserEmailException()

        val review = Review(
            user = user,
            rating = reviewRequest.rating,
            content = reviewRequest.comment,
            isLike = reviewRequest.isLike,
            restaurantId = restaurantId
        )

        val reviewImages = reviewRequest.imageUrls.map { imageUrl ->
            ReviewImage(
                imageUrl = imageUrl,
            )
        }
        reviewImages.forEach { review.addImage(it) }

        return CreateReviewResponse(review = reviewRepository.save(review));
    }
}
package com.restaurant.be.review.domain.service

import com.restaurant.be.common.exception.NotFoundReviewException
import com.restaurant.be.common.exception.NotFoundUserEmailException
import com.restaurant.be.review.domain.entity.QReview.review
import com.restaurant.be.review.domain.entity.ReviewImage
import com.restaurant.be.review.presentation.dto.CreateReviewResponse
import com.restaurant.be.review.presentation.dto.common.ReviewRequestDto
import com.restaurant.be.review.presentation.dto.common.ReviewResponseDto
import com.restaurant.be.review.repository.ReviewRepository
import com.restaurant.be.user.repository.UserRepository
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class CreateReviewService(
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository
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

        val reviewWithLikes = reviewRepository.findReview(user, review)
            ?: throw NotFoundReviewException()

        val responseDto = ReviewResponseDto.toDto(
            reviewWithLikes.review,
            reviewWithLikes.isLikedByUser
        )

        return CreateReviewResponse(responseDto)
    }
}

package com.restaurant.be.review.domain.service

import com.restaurant.be.common.exception.NotFoundUserEmailException
import com.restaurant.be.review.domain.entity.ReviewImage
import com.restaurant.be.review.presentation.dto.CreateReviewResponse
import com.restaurant.be.review.presentation.dto.common.ReviewRequestDto
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

        return CreateReviewResponse(review = reviewRepository.save(review).toResponseDTO())
    }
}

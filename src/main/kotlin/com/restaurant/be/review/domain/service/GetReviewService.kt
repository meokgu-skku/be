package com.restaurant.be.review.domain.service

import com.restaurant.be.common.exception.NotFoundReviewException
import com.restaurant.be.common.exception.NotFoundUserEmailException
import com.restaurant.be.review.presentation.dto.GetMyReviewsResponse
import com.restaurant.be.review.presentation.dto.GetReviewResponse
import com.restaurant.be.review.presentation.dto.GetReviewsResponse
import com.restaurant.be.review.presentation.dto.ReviewWithLikesDto
import com.restaurant.be.review.presentation.dto.common.ReviewResponseDto
import com.restaurant.be.review.repository.ReviewRepository
import com.restaurant.be.user.repository.UserRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetReviewService(
    private val userRepository: UserRepository,
    private val reviewRepository: ReviewRepository

) {
    @Transactional(readOnly = true)
    fun getReviews(pageable: Pageable, restaurantId: Long, email: String): GetReviewsResponse {
        val user = userRepository.findByEmail(email)
            ?: throw NotFoundUserEmailException()

        val reviewsWithLikes = reviewRepository.findReviews(user, restaurantId, pageable)

        val responseDtos = convertResponeDto(reviewsWithLikes)

        return GetReviewsResponse(responseDtos, pageable)
    }

    @Transactional
    fun getReview(reviewId: Long, email: String): GetReviewResponse {
        val user = userRepository.findByEmail(email)
            ?: throw NotFoundUserEmailException()

        val reviewWithLikes = reviewRepository.findReview(user, reviewId)
            ?: throw NotFoundReviewException()

        if (reviewWithLikes.review.user.id != user.id) {
            reviewWithLikes.review.incrementViewCount()
        }

        val responseDto = ReviewResponseDto.toDto(
            reviewWithLikes.review,
            reviewWithLikes.isLikedByUser
        )

        return GetReviewResponse(responseDto)
    }

    @Transactional(readOnly = true)
    fun getMyReviews(pageable: Pageable, email: String): GetMyReviewsResponse {
        val user = userRepository.findByEmail(email)
            ?: throw NotFoundUserEmailException()

        val reviewsWithLikes = reviewRepository.findMyReviews(user, pageable)

        val reviewResponses = convertResponeDto(reviewsWithLikes)

        return GetMyReviewsResponse(reviewResponses, pageable)
    }

    private fun convertResponeDto(reviewsWithLikes: List<ReviewWithLikesDto>): List<ReviewResponseDto> {
        val reviewResponses = reviewsWithLikes.map {
            ReviewResponseDto.toDto(
                it.review,
                it.isLikedByUser
            )
        }
        return reviewResponses
    }
}

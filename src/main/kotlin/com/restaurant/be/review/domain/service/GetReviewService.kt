package com.restaurant.be.review.domain.service

import com.restaurant.be.common.exception.NotFoundReviewException
import com.restaurant.be.common.exception.NotFoundUserEmailException
import com.restaurant.be.review.presentation.dto.GetOneReviewResponse
import com.restaurant.be.review.presentation.dto.GetReviewResponse
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
    fun getReviews(pageable: Pageable, email: String): GetReviewResponse {
        val user = userRepository.findByEmail(email)
            ?: throw NotFoundUserEmailException()

        val reviewsWithLikes = reviewRepository.findReviews(user, pageable)

        val responseDtos = reviewsWithLikes.map {
            ReviewResponseDto.toDto(
                it.review,
                it.isLikedByUser
            )
        }

        return GetReviewResponse(responseDtos)
    }

    @Transactional
    fun getOneReview(reviewId: Long, email: String): GetOneReviewResponse {
        val user = userRepository.findByEmail(email)
            ?: throw NotFoundUserEmailException()

        val reviewWithLikes = reviewRepository.findReview(user, reviewId)
            ?: throw NotFoundReviewException()

        val responseDto = ReviewResponseDto.toDto(
            reviewWithLikes!!.review,
            reviewWithLikes.isLikedByUser
        )

        return GetOneReviewResponse(responseDto)
    }
}

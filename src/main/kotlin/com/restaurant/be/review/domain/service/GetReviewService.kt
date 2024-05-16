package com.restaurant.be.review.domain.service

import com.restaurant.be.common.exception.NotFoundUserEmailException
import com.restaurant.be.review.presentation.dto.GetReviewResponse
import com.restaurant.be.review.repository.ReviewLikesRepository
import com.restaurant.be.review.repository.ReviewRepository
import com.restaurant.be.user.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class GetReviewService(
    private val userRepository: UserRepository,
    private val reviewRepository: ReviewRepository,
    private val reviewLikesRepository: ReviewLikesRepository
) {
    fun getReviewListOf(page: Int, size: Int, email: String): GetReviewResponse {
        val pageable = PageRequest.of(page, size)
        val reviews = reviewRepository.findAll(pageable).content


        val user = userRepository.findByEmail(email)
            ?: throw NotFoundUserEmailException()


        return GetReviewResponse(
            reviews.map {
                it
                    .toResponseDTO(doesUserLike = isReviewLikedByUser(user.id, it.id))
            }
        )
    }

    fun isReviewLikedByUser(userId: Long?, reviewId: Long?): Boolean {
        if (userId != 0L) {
            return reviewLikesRepository.existsByReviewIdAndUserId(userId, reviewId)
        }
        return false
    }
}

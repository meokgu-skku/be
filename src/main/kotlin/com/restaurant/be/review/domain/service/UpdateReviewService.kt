package com.restaurant.be.review.domain.service

import com.restaurant.be.common.exception.NotFoundReviewException
import com.restaurant.be.common.exception.NotFoundUserEmailException
import com.restaurant.be.common.exception.UnAuthorizedUpdateException
import com.restaurant.be.restaurant.repository.RestaurantRepository
import com.restaurant.be.review.presentation.dto.UpdateReviewRequest
import com.restaurant.be.review.presentation.dto.UpdateReviewResponse
import com.restaurant.be.review.repository.ReviewLikeRepository
import com.restaurant.be.review.repository.ReviewRepository
import com.restaurant.be.user.repository.UserRepository
import org.springframework.stereotype.Service
import javax.transaction.Transactional
import kotlin.jvm.optionals.getOrNull

@Service
class UpdateReviewService(
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository,
    private val restaurantRepository: RestaurantRepository,
    private val reviewLikeRepository: ReviewLikeRepository
) {
    @Transactional
    fun updateReview(
        restaurantId: Long,
        reviewId: Long,
        reviewRequest: UpdateReviewRequest,
        email: String
    ): UpdateReviewResponse {
        val user = userRepository.findByEmail(email) ?: throw NotFoundUserEmailException()

        val review = reviewRepository.findById(reviewId)
            .orElseThrow { NotFoundReviewException() }

        if (user.id != review.user.id) throw UnAuthorizedUpdateException()

        applyReviewCountAndAvgRating(
            review.restaurantId,
            review.rating,
            reviewRequest.review.rating
        )

        review.updateReview(reviewRequest)

        reviewRepository.save(review)

        val responseDto =
            review.toDto(reviewLikeRepository.existsByReviewIdAndUserId(reviewId, user.id))

        return UpdateReviewResponse(responseDto)
    }

    private fun applyReviewCountAndAvgRating(
        restaurantId: Long,
        rating: Double,
        updateRating: Double
    ) {
        val restaurant = restaurantRepository.findById(restaurantId).getOrNull()
        if (restaurant != null) {
            restaurant.updateReview(rating, updateRating)
            restaurantRepository.save(restaurant)
        }
    }
}

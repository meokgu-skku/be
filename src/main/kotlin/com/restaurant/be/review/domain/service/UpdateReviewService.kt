package com.restaurant.be.review.domain.service

import com.restaurant.be.common.exception.NotFoundRestaurantException
import com.restaurant.be.common.exception.NotFoundReviewException
import com.restaurant.be.common.exception.NotFoundUserEmailException
import com.restaurant.be.common.exception.UnAuthorizedUpdateException
import com.restaurant.be.restaurant.repository.RestaurantRepository
import com.restaurant.be.review.domain.entity.QReview.review
import com.restaurant.be.review.presentation.dto.UpdateReviewRequest
import com.restaurant.be.review.presentation.dto.UpdateReviewResponse
import com.restaurant.be.review.presentation.dto.common.ReviewResponseDto
import com.restaurant.be.review.repository.ReviewRepository
import com.restaurant.be.user.repository.UserRepository
import org.springframework.stereotype.Service
import javax.transaction.Transactional
import kotlin.jvm.optionals.getOrNull

@Service
class UpdateReviewService(
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository,
    private val restaurantRepository: RestaurantRepository
) {
    @Transactional
    fun updateReview(restaurantId: Long, reviewId: Long, reviewRequest: UpdateReviewRequest, email: String): UpdateReviewResponse {
        val user = userRepository.findByEmail(email)
            ?: throw NotFoundUserEmailException()

        val review = reviewRepository.findById(reviewId)
            .getOrNull()
            ?: throw NotFoundReviewException()

        if (user.id != review.user.id) throw UnAuthorizedUpdateException()

        applyReviewCountAndAvgRating(review.restaurantId, review.rating, reviewRequest.review.rating)

        review.updateReview(reviewRequest)

        val reviewWithLikes = reviewRepository.findReview(user, reviewId)
            ?: throw NotFoundReviewException()

        val responseDto = ReviewResponseDto.toDto(
            reviewWithLikes.review,
            reviewWithLikes.isLikedByUser
        )

        return UpdateReviewResponse(responseDto)
    }

    private fun applyReviewCountAndAvgRating(restaurantId: Long, rating: Double, updateRating: Double) {
        val restaurant = restaurantRepository.findById(restaurantId).getOrNull()
            ?: throw NotFoundRestaurantException()
        val reviewCount = restaurant.reviewCount

        restaurant.ratingAvg = (restaurant.ratingAvg * reviewCount - rating + updateRating) / reviewCount 
    }
}

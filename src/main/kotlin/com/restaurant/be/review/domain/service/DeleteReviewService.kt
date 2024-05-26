package com.restaurant.be.review.domain.service

import com.restaurant.be.common.exception.NotFoundRestaurantException
import com.restaurant.be.common.exception.NotFoundReviewException
import com.restaurant.be.common.exception.NotFoundUserEmailException
import com.restaurant.be.common.exception.UnAuthorizedDeleteException
import com.restaurant.be.restaurant.repository.RestaurantRepository
import com.restaurant.be.review.domain.entity.QReview.review
import com.restaurant.be.review.repository.ReviewRepository
import com.restaurant.be.user.repository.UserRepository
import org.springframework.stereotype.Service
import javax.transaction.Transactional
import kotlin.jvm.optionals.getOrNull

@Service
class DeleteReviewService(
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository,
    private val restaurantRepository: RestaurantRepository
) {
    @Transactional
    fun deleteReview(reviewId: Long, email: String) {
        val user = userRepository.findByEmail(email) ?: throw NotFoundUserEmailException()

        var review = reviewRepository.findById(reviewId).getOrNull() ?: throw NotFoundReviewException()

        applyReviewCountAndAvgRating(review.restaurantId)

        if (user.id != review.user.id) throw UnAuthorizedDeleteException()

        reviewRepository.deleteById(reviewId)
    }

    private fun applyReviewCountAndAvgRating(restaurantId: Long) {
        val restaurant = restaurantRepository.findById(restaurantId).getOrNull()
            ?: throw NotFoundRestaurantException()
        val beforeCount = restaurant.reviewCount
        if (beforeCount <= 1) {
            restaurant.ratingAvg = 0.0
            restaurant.reviewCount = 0
        } else {
            restaurant.ratingAvg = (restaurant.ratingAvg * beforeCount) / (beforeCount - 1)
            restaurant.reviewCount = beforeCount - 1
        }
    }
}

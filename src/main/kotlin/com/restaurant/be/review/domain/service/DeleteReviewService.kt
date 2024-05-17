package com.restaurant.be.review.domain.service

import com.restaurant.be.common.exception.NotFoundReviewException
import com.restaurant.be.common.exception.NotFoundUserEmailException
import com.restaurant.be.common.exception.UnAuthorizedUpdateException
import com.restaurant.be.review.repository.ReviewRepository
import com.restaurant.be.user.repository.UserRepository
import org.springframework.stereotype.Service
import javax.transaction.Transactional
import kotlin.jvm.optionals.getOrNull

@Service
class DeleteReviewService(
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository
) {
    @Transactional
    fun deleteReview(reviewId: Long, email: String) {
        val user = userRepository.findByEmail(email) ?: throw NotFoundUserEmailException()

        var review = reviewRepository.findById(reviewId).getOrNull() ?: throw NotFoundReviewException()

        if (user.id != review.user.id) throw UnAuthorizedUpdateException()

        reviewRepository.deleteById(reviewId)
    }
}

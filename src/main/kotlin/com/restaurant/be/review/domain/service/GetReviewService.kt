package com.restaurant.be.review.domain.service

import com.restaurant.be.common.exception.NotFoundUserEmailException
import com.restaurant.be.common.principal.PrincipalUtils
import com.restaurant.be.review.presentation.dto.GetReviewResponse
import com.restaurant.be.review.repository.ReviewLikesRepository
import com.restaurant.be.review.repository.ReviewRepository
import com.restaurant.be.user.domain.entity.QUser.user
import com.restaurant.be.user.repository.UserRepository
import kotlinx.serialization.json.JsonNull.content
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

        var userId = 0L
        if (!PrincipalUtils.isAnonymous(email)) {
            val user = userRepository.findByEmail(email)
                ?: throw NotFoundUserEmailException()
            userId = user.id ?: 0L
        }

        return GetReviewResponse(
            reviews.map {
                it
                    .toResponseDTO(doesUserLike = isReviewLikedByUser(userId, it.id))
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

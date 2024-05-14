package com.restaurant.be.review.domain.service

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import com.restaurant.be.common.exception.NotFoundUserEmailException
import com.restaurant.be.common.principal.PrincipalUtils
import com.restaurant.be.review.domain.entity.QReview
import com.restaurant.be.review.domain.entity.QReviewLikes
import com.restaurant.be.review.domain.entity.Review
import com.restaurant.be.review.presentation.dto.GetReviewResponse
import com.restaurant.be.review.presentation.dto.ReviewWithLikesDto
import com.restaurant.be.review.presentation.dto.common.ReviewResponseDto
import com.restaurant.be.review.repository.ReviewLikesRepository
import com.restaurant.be.review.repository.ReviewRepository
import com.restaurant.be.user.domain.entity.QUser.user
import com.restaurant.be.user.domain.entity.User
import com.restaurant.be.user.repository.UserRepository
import org.springframework.transaction.annotation.Transactional
import kotlinx.serialization.json.JsonNull.content
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class GetReviewService(
    private val userRepository: UserRepository,
    private val reviewRepository: ReviewRepository,
    private val jpaQueryFactory: JPAQueryFactory
) {
    @Transactional(readOnly = true)
    fun getReviewList(pageable: Pageable, email: String): GetReviewResponse {
        val user = userRepository.findByEmail(email)
            ?: throw NotFoundUserEmailException()

        val reviews = reviewRepository.findAll(pageable)

        val reviewsWithLikes = joinQuery(user, reviews.content)

        val reviewResponses = reviewsWithLikes.map {
            ReviewResponseDto.toDto(
                it!!.review,
                it!!.isLikedByUser
            )
        }
        return GetReviewResponse(reviewResponses)
    }

    private fun joinQuery(user: User, reviews: List<Review>): List<ReviewWithLikesDto> {
        val reviewIds = reviews.map { it.id } // 리뷰 ID 목록 가져오기

        val reviewsWithLikes = jpaQueryFactory
            .from(QReview.review)
            .leftJoin(QReviewLikes.reviewLikes)
            .on(
                QReviewLikes.reviewLikes.reviewId.eq(QReview.review.id)
                    .and(QReviewLikes.reviewLikes.userId.eq(user.id))
            )
            .where(QReview.review.id.`in`(reviewIds)) // 해당 리뷰 ID 목록에 속하는 리뷰만 가져오기
            .select(
                Projections.constructor(
                    ReviewWithLikesDto::class.java,
                    QReview.review,
                    QReviewLikes.reviewLikes.userId.isNotNull()
                )
            )
            .fetch()

        return reviewsWithLikes
    }
}

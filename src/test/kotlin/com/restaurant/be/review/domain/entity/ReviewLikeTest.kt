package com.restaurant.be.review.domain.entity

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ReviewLikeTest : DescribeSpec({

    describe("ReviewLike") {

        describe("entity mapping") {
            it("should map fields correctly") {
                // Given
                val userId = 1L
                val reviewId = 100L

                // When
                val reviewLike = ReviewLike(
                    userId = userId,
                    reviewId = reviewId
                )

                // Then
                reviewLike.userId shouldBe userId
                reviewLike.reviewId shouldBe reviewId
            }
        }
    }
})

package com.restaurant.be.review.domain.entity

import com.restaurant.be.common.exception.InvalidLikeCountException
import com.restaurant.be.user.domain.entity.User
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ReviewTest : DescribeSpec({

    describe("Review") {

        describe("decrementLikeCount") {
            it("should decrement like count when it is greater than zero") {
                // Given
                val user = User(
                    id = 1L,
                    nickname = "testuser",
                    profileImageUrl = "http://example.com/profile.jpg"
                )
                val review = Review(
                    user = user,
                    restaurantId = 10L,
                    content = "Great restaurant!",
                    rating = 4.5,
                    likeCount = 5,
                    viewCount = 100
                )

                // When
                review.decrementLikeCount()

                // Then
                review.likeCount shouldBe 4
            }

            it("should throw InvalidLikeCountException when like count is zero") {
                // Given
                val user = User(
                    id = 1L,
                    nickname = "testuser",
                    profileImageUrl = "http://example.com/profile.jpg"
                )
                val review = Review(
                    user = user,
                    restaurantId = 10L,
                    content = "Great restaurant!",
                    rating = 4.5,
                    likeCount = 0,
                    viewCount = 100
                )

                // When / Then
                shouldThrow<InvalidLikeCountException> {
                    review.decrementLikeCount()
                }
            }
        }
    }
})

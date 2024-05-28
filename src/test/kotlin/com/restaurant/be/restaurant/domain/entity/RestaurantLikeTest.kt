package com.restaurant.be.restaurant.domain.entity

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class RestaurantLikeTest : DescribeSpec({
    describe("RestaurantLike") {
        it("should create a correct RestaurantLike instance") {
            // Given
            val restaurantLike = RestaurantLike(
                id = 1L,
                restaurantId = 1,
                userId = 1
            )

            // When

            // Then
            restaurantLike.id shouldBe 1L
            restaurantLike.restaurantId shouldBe 1
            restaurantLike.userId shouldBe 1
        }
    }
})

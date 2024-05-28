package com.restaurant.be.restaurant.domain.entity

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class RestaurantCategoryTest : DescribeSpec({

    describe("RestaurantCategory") {
        it("should create a correct RestaurantCategory instance") {
            // Given
            val restaurantCategory = RestaurantCategory(
                id = 1L,
                restaurantId = 1,
                categoryId = 1
            )

            // When

            // Then
            restaurantCategory.id shouldBe 1L
            restaurantCategory.restaurantId shouldBe 1
            restaurantCategory.categoryId shouldBe 1
        }
    }
})

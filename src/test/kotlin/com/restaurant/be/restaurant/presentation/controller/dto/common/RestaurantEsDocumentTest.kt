package com.restaurant.be.restaurant.presentation.controller.dto.common

import com.restaurant.be.restaurant.repository.dto.RestaurantEsDocument
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class RestaurantEsDocumentTest : DescribeSpec({
    describe("MenuDto") {
        it("should create a correct RestaurantEsDocument instance") {
            // Given
            val restaurantEsDocument = RestaurantEsDocument(
                id = 1L,
                name = "Pizza",
                originalCategory = "Italian",
                naverReviewCount = 100,
                address = "Seoul",
                naverRatingAvg = 4.5,
                imageUrl = "http://example.com/pizza.jpg",
                category = "Italian",
                discountContent = "10% off",
                menus = mutableListOf(),
                reviewCount = 1L,
                ratingAvg = 4.5
            )

            // When

            // Then
            restaurantEsDocument.id shouldBe 1L
            restaurantEsDocument.name shouldBe "Pizza"
            restaurantEsDocument.originalCategory shouldBe "Italian"
            restaurantEsDocument.naverReviewCount shouldBe 100
            restaurantEsDocument.address shouldBe "Seoul"
            restaurantEsDocument.naverRatingAvg shouldBe 4.5
            restaurantEsDocument.imageUrl shouldBe "http://example.com/pizza.jpg"
            restaurantEsDocument.category shouldBe "Italian"
            restaurantEsDocument.discountContent shouldBe "10% off"
            restaurantEsDocument.menus shouldBe mutableListOf()
            restaurantEsDocument.reviewCount shouldBe 1L
            restaurantEsDocument.ratingAvg shouldBe 4.5
        }

        it("should create a correct MenuDto instance") {
            // Given
            val menuEsDocument = MenuDto(
                name = "Pasta",
                price = 15000,
                description = "Delicious pasta",
                isRepresentative = true,
                imageUrl = "http://example.com/pasta.jpg"
            )

            // When

            // Then
            menuEsDocument.name shouldBe "Pasta"
            menuEsDocument.price shouldBe 15000
            menuEsDocument.description shouldBe "Delicious pasta"
            menuEsDocument.isRepresentative shouldBe true
            menuEsDocument.imageUrl shouldBe "http://example.com/pasta.jpg"
        }
    }
})

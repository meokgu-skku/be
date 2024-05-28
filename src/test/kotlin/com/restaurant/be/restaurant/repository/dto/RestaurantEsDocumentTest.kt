package com.restaurant.be.restaurant.repository.dto

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class RestaurantEsDocumentTest : DescribeSpec({
    describe("RestaurantEsDocument") {
        it("should create a correct RestaurantEsDocument instance") {
            // Given
            val menuEsDocument = MenuEsDocument(
                restaurantId = 1,
                menuName = "Pasta",
                price = 15000,
                description = "Delicious pasta",
                imageUrl = "http://example.com/pasta.jpg"
            )
            val restaurantEsDocument = RestaurantEsDocument(
                id = 1L,
                name = "Test Restaurant",
                originalCategory = "Italian",
                naverReviewCount = 100L,
                address = "123 Test St",
                naverRatingAvg = 4.5,
                imageUrl = "http://example.com/restaurant.jpg",
                category = "Italian, Pizza",
                discountContent = "10% off",
                menus = listOf(menuEsDocument),
                reviewCount = 200L,
                ratingAvg = 4.5
            )

            // When

            // Then
            restaurantEsDocument.id shouldBe 1L
            restaurantEsDocument.name shouldBe "Test Restaurant"
            restaurantEsDocument.originalCategory shouldBe "Italian"
            restaurantEsDocument.naverReviewCount shouldBe 100L
            restaurantEsDocument.address shouldBe "123 Test St"
            restaurantEsDocument.naverRatingAvg shouldBe 4.5
            restaurantEsDocument.imageUrl shouldBe "http://example.com/restaurant.jpg"
            restaurantEsDocument.category shouldBe "Italian, Pizza"
            restaurantEsDocument.discountContent shouldBe "10% off"
            restaurantEsDocument.menus.size shouldBe 1
            restaurantEsDocument.menus[0].menuName shouldBe "Pasta"
            restaurantEsDocument.reviewCount shouldBe 200L
            restaurantEsDocument.ratingAvg shouldBe 4.5
        }
    }

    describe("MenuEsDocument") {
        it("should create a correct MenuEsDocument instance") {
            // Given
            val menuEsDocument = MenuEsDocument(
                restaurantId = 1,
                menuName = "Pasta",
                price = 15000,
                description = "Delicious pasta",
                imageUrl = "http://example.com/pasta.jpg"
            )

            // When

            // Then
            menuEsDocument.restaurantId shouldBe 1
            menuEsDocument.menuName shouldBe "Pasta"
            menuEsDocument.price shouldBe 15000
            menuEsDocument.description shouldBe "Delicious pasta"
            menuEsDocument.imageUrl shouldBe "http://example.com/pasta.jpg"
        }
    }
})

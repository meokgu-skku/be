package com.restaurant.be.restaurant.domain.entity

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class MenuTest : DescribeSpec({

    describe("Menu entity") {
        it("should create a correct Menu instance") {
            // Arrange
            val menu = Menu(
                id = 1L,
                restaurantId = 1L,
                name = "Pizza",
                price = 1500,
                description = "Delicious cheese pizza",
                isRepresentative = true,
                imageUrl = "http://example.com/pizza.jpg"
            )

            // Assert
            menu.id shouldBe 1L
            menu.restaurantId shouldBe 1L
            menu.name shouldBe "Pizza"
            menu.price shouldBe 1500
            menu.description shouldBe "Delicious cheese pizza"
            menu.isRepresentative shouldBe true
            menu.imageUrl shouldBe "http://example.com/pizza.jpg"
        }

        it("should correctly convert to MenuDto") {
            // Arrange
            val menu = Menu(
                id = 1L,
                restaurantId = 1L,
                name = "Pizza",
                price = 1500,
                description = "Delicious cheese pizza",
                isRepresentative = true,
                imageUrl = "http://example.com/pizza.jpg"
            )

            // Act
            val menuDto = menu.toDto()

            // Assert
            menuDto.name shouldBe "Pizza"
            menuDto.price shouldBe 1500
            menuDto.description shouldBe "Delicious cheese pizza"
            menuDto.isRepresentative shouldBe true
            menuDto.imageUrl shouldBe "http://example.com/pizza.jpg"
        }
    }
})

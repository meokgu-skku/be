package com.restaurant.be.common.entity

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.LocalDateTime

class BaseEntityTest : DescribeSpec({

    describe("BaseEntity") {

        it("should set createdAt and modifiedAt on instantiation") {
            // When
            val baseEntity = BaseEntity()

            // Then
            baseEntity.createdAt shouldNotBe null
            baseEntity.modifiedAt shouldNotBe null
        }

        it("should allow setting createdAt and modifiedAt manually") {
            // Given
            val baseEntity = BaseEntity()
            val newCreatedAt = LocalDateTime.now()
            val newModifiedAt = LocalDateTime.now()

            // When
            baseEntity.createdAt = newCreatedAt
            baseEntity.modifiedAt = newModifiedAt

            // Then
            baseEntity.createdAt shouldBe newCreatedAt
            baseEntity.modifiedAt shouldBe newModifiedAt
        }
    }
})

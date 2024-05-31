package com.restaurant.be.common.response

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ErrorCodeTest : DescribeSpec({

    describe("ErrorCode") {

        it("should have the correct message for COMMON_NULL_PARAMETER") {
            // Given
            val errorCode = ErrorCode.COMMON_NULL_PARAMETER

            // Then
            errorCode.errorMsg shouldBe "빠뜨린 값이 없는지 확인 해주세요."
        }

        it("should have the correct message for COMMON_INVALID_PARAMETER") {
            // Given
            val errorCode = ErrorCode.COMMON_INVALID_PARAMETER

            // Then
            errorCode.errorMsg shouldBe "요청한 값이 올바르지 않습니다."
        }
    }
})

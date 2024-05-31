package com.restaurant.be.common.response

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class CommonResponseTest : DescribeSpec({

    describe("CommonResponse") {

        it("should create a success response with data") {
            // Given
            val data = "testData"

            // When
            val response = CommonResponse.success(data)

            // Then
            response.result shouldBe CommonResponse.Result.SUCCESS
            response.data shouldBe data
            response.message shouldBe ""
            response.errorCode shouldBe null
        }

        it("should create a success response without data") {
            // When
            val response = CommonResponse.success<Any>()

            // Then
            response.result shouldBe CommonResponse.Result.SUCCESS
            response.data shouldBe null
            response.message shouldBe ""
            response.errorCode shouldBe null
        }

        it("should create a fail response with data and message") {
            // Given
            val message = "testMessage"

            // When
            val response = CommonResponse.fail(null, message)

            // Then
            response.result shouldBe CommonResponse.Result.FAIL
            response.data shouldBe null
            response.message shouldBe message
            response.errorCode shouldBe null
        }

        it("should create a fail response with data and error code") {
            // Given
            val data = "testData"
            val errorCode = ErrorCode.COMMON_INVALID_PARAMETER

            // When
            val response = CommonResponse.fail(data, errorCode)

            // Then
            response.result shouldBe CommonResponse.Result.FAIL
            response.data shouldBe data
            response.message shouldBe errorCode.errorMsg
            response.errorCode shouldBe errorCode.name
        }

        it("should create a fail response with message and exception name") {
            // Given
            val message = "testMessage"
            val exceptionName = "TestException"

            // When
            val response = CommonResponse.fail(message, exceptionName)

            // Then
            response.result shouldBe CommonResponse.Result.FAIL
            response.data shouldBe null
            response.message shouldBe message
            response.errorCode shouldBe exceptionName
        }
    }

    describe("Error") {
        it("should create an error with field, message, and value") {
            // Given
            val field = "testField"
            val message = "testMessage"
            val value = "testValue"

            // When
            val error = Error(field, message, value)

            // Then
            error.field shouldBe field
            error.message shouldBe message
            error.value shouldBe value
        }
    }
})

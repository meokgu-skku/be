package com.restaurant.be.common.exception

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ServerExceptionTest : DescribeSpec({
    describe("UnAuthorizedDeleteException") {
        it("should have the correct code and message") {
            val exception = UnAuthorizedDeleteException()

            exception.code shouldBe 401
            exception.message shouldBe "해당 게시글을 삭제할 권한이 없습니다."
        }

        it("should throw UnAuthorizedDeleteException with default message") {
            val exception = shouldThrow<UnAuthorizedDeleteException> {
                throw UnAuthorizedDeleteException()
            }

            exception.code shouldBe 401
            exception.message shouldBe "해당 게시글을 삭제할 권한이 없습니다."
        }

        it("should throw UnAuthorizedDeleteException with custom message") {
            val customMessage = "Custom unauthorized delete message."
            val exception = shouldThrow<UnAuthorizedDeleteException> {
                throw UnAuthorizedDeleteException(customMessage)
            }

            exception.code shouldBe 401
            exception.message shouldBe customMessage
        }
    }
})

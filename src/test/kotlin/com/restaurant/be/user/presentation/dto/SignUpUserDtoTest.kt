package com.restaurant.be.user.presentation.dto

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty

class SignUpUserDtoTest : DescribeSpec({
    describe("SignUpUserRequest") {
        context("when valid parameters are provided") {
            it("should create a valid User entity") {
                val user = SignUpUserRequest(
                    email = "test@gmail.com",
                    password = "test12!@",
                    nickname = "닉네임",
                    profileImageUrl = "https://test.com/test.jpg"
                )

                user.email shouldBe "test@gmail.com"
                user.password.shouldNotBeEmpty()
                user.nickname shouldBe "닉네임"
                user.profileImageUrl shouldBe "https://test.com/test.jpg"
            }
        }
    }
})

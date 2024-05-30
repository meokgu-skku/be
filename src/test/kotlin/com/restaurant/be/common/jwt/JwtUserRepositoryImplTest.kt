package com.restaurant.be.common.jwt

import com.restaurant.be.common.exception.NotFoundUserEmailException
import com.restaurant.be.common.exception.WithdrawalUserException
import com.restaurant.be.user.domain.entity.User
import com.restaurant.be.user.repository.UserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class JwtUserRepositoryImplTest : DescribeSpec({

    val userRepository = mockk<UserRepository>()
    val jwtUserRepository = JwtUserRepositoryImpl(userRepository)

    describe("JwtUserRepositoryImpl") {

        describe("validTokenByEmail") {
            it("should return true if user is found and not withdrawn") {
                // Given
                val email = "test@example.com"
                val user = User(email = email, withdrawal = false, profileImageUrl = "")
                every { userRepository.findByEmail(email) } returns user

                // When
                val result = jwtUserRepository.validTokenByEmail(email)

                // Then
                result shouldBe true
            }

            it("should return false if user is not found") {
                // Given
                val email = "test@example.com"
                every { userRepository.findByEmail(email) } returns null

                // When
                val result = jwtUserRepository.validTokenByEmail(email)

                // Then
                result shouldBe false
            }

            it("should return false if user is withdrawn") {
                // Given
                val email = "test@example.com"
                val user = User(email = email, withdrawal = true, profileImageUrl = "")
                every { userRepository.findByEmail(email) } returns user

                // When
                val result = jwtUserRepository.validTokenByEmail(email)

                // Then
                result shouldBe false
            }
        }

        describe("userRolesByEmail") {
            it("should return user roles if user is found and not withdrawn") {
                // Given
                val email = "test@example.com"
                val roles = listOf("ROLE_USER", "ROLE_ADMIN")
                val user =
                    User(email = email, withdrawal = false, roles = roles, profileImageUrl = "")
                every { userRepository.findByEmail(email) } returns user

                // When
                val result = jwtUserRepository.userRolesByEmail(email)

                // Then
                result shouldBe roles
            }

            it("should throw NotFoundUserEmailException if user is not found") {
                // Given
                val email = "test@example.com"
                every { userRepository.findByEmail(email) } returns null

                // When / Then
                shouldThrow<NotFoundUserEmailException> {
                    jwtUserRepository.userRolesByEmail(email)
                }
            }

            it("should throw WithdrawalUserException if user is withdrawn") {
                // Given
                val email = "test@example.com"
                val user = User(email = email, withdrawal = true, profileImageUrl = "")
                every { userRepository.findByEmail(email) } returns user

                // When / Then
                shouldThrow<WithdrawalUserException> {
                    jwtUserRepository.userRolesByEmail(email)
                }
            }
        }
    }
})

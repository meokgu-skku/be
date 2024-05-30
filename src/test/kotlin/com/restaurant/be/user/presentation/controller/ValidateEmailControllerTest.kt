package com.restaurant.be.user.presentation.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.restaurant.be.common.CustomDescribeSpec
import com.restaurant.be.common.IntegrationTest
import com.restaurant.be.common.PageDeserializer
import com.restaurant.be.common.exception.InvalidEmailCodeException
import com.restaurant.be.common.exception.SendEmailException
import com.restaurant.be.common.exception.SkkuEmailException
import com.restaurant.be.common.redis.RedisRepository
import com.restaurant.be.common.response.CommonResponse
import com.restaurant.be.common.util.setUpUser
import com.restaurant.be.restaurant.presentation.controller.dto.common.RestaurantDto
import com.restaurant.be.user.domain.service.ValidateEmailService
import com.restaurant.be.user.presentation.dto.SendEmailRequest
import com.restaurant.be.user.presentation.dto.ValidateEmailRequest
import com.restaurant.be.user.presentation.dto.common.EmailSendType
import com.restaurant.be.user.repository.EmailRepository
import com.restaurant.be.user.repository.UserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.domain.Page
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.transaction.annotation.Transactional

@IntegrationTest
@Transactional
class ValidateEmailControllerTest(
    private val userRepository: UserRepository,
    private val redisTemplate: RedisTemplate<String, String>,
    private val redisRepository: RedisRepository
) : CustomDescribeSpec() {
    private val emailRepository: EmailRepository = mockk()
    private val validateEmailService = ValidateEmailService(emailRepository, "", redisRepository)
    private val validateEmailController = ValidateEmailController(validateEmailService)

    private val baseUrl = "/v1/users/email"
    private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule()).apply {
        val module = SimpleModule()
        module.addDeserializer(Page::class.java, PageDeserializer(RestaurantDto::class.java))
        this.registerModule(module)
    }

    init {
        beforeEach {
            setUpUser("test@gmail.com", userRepository)
        }

        afterEach {
            redisTemplate.keys("*").forEach { redisTemplate.delete(it) }
        }

        describe("#sendEmail") {
            it("when sign up email success should return 200") {
                // given
                val request =
                    SendEmailRequest(email = "test@g.skku.edu", sendType = EmailSendType.SIGN_UP)

                every { emailRepository.generateRandomCode() } returns "123456"
                every { emailRepository.sendEmail(any()) } returns Unit

                // when
                val result = validateEmailController.sendEmail(request)

                // then
                result.result shouldBe CommonResponse.Result.SUCCESS
            }

            it("when reset password email success should return 200") {
                // given
                val request =
                    SendEmailRequest(
                        email = "test@g.skku.edu",
                        sendType = EmailSendType.RESET_PASSWORD
                    )

                every { emailRepository.generateRandomCode() } returns "123456"
                every { emailRepository.sendEmail(any()) } returns Unit

                // when
                val result = validateEmailController.sendEmail(request)

                // then
                result.result shouldBe CommonResponse.Result.SUCCESS
            }

            it("when invalid email should return 400") {
                // given
                val request =
                    SendEmailRequest(email = "test@gmail.com", sendType = EmailSendType.SIGN_UP)

                // then
                shouldThrow<SkkuEmailException> {
                    // when
                    validateEmailController.sendEmail(request)
                }
            }

            it("when email sender fail should return 400") {
                // given
                val request =
                    SendEmailRequest(email = "test@g.skku.edu", sendType = EmailSendType.SIGN_UP)

                every { emailRepository.generateRandomCode() } returns "123456"
                every { emailRepository.sendEmail(any()) } throws Exception()

                // then
                shouldThrow<SendEmailException> {
                    // when
                    validateEmailController.sendEmail(request)
                }
            }
        }

        describe("#validateEmail") {
            it("when valid code success should return 200") {
                // given
                val request = ValidateEmailRequest(
                    email = "test@test.com",
                    code = "123456",
                    sendType = EmailSendType.SIGN_UP
                )
                redisTemplate.opsForValue().set(
                    "user:${request.email}:${request.sendType.name.lowercase()}_code",
                    "123456"
                )

                // when
                val result = validateEmailController.validateEmail(request)

                // then
                result.result shouldBe CommonResponse.Result.SUCCESS
            }

            it("when invalid code should return InvalidEmailCodeException") {
                // given
                val request = ValidateEmailRequest(
                    email = "test@gmail.com",
                    code = "123456",
                    sendType = EmailSendType.SIGN_UP
                )
                redisTemplate.opsForValue().set(
                    "user:${request.email}:${request.sendType.name.lowercase()}_code",
                    "1234567"
                )

                // then
                shouldThrow<InvalidEmailCodeException> {
                    // when
                    validateEmailController.validateEmail(request)
                }
            }
        }
    }
}

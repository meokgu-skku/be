package com.restaurant.be.common.exception

import club.minnced.discord.webhook.WebhookClient
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.web.server.ServerWebInputException
import java.security.SignatureException
import javax.servlet.http.HttpServletRequest

class GlobalExceptionHandlerTest : DescribeSpec({

    val webhookClient = mockk<WebhookClient>(relaxed = true)
    val request = mockk<HttpServletRequest>()
    val exceptionHandler = GlobalExceptionHandler(webhookClient)

    describe("GlobalExceptionHandler") {

        describe("methodArgumentNotValidException for ServerWebInputException") {
            it("should handle MissingKotlinParameterException") {
                val exception = ServerWebInputException("")

                every { request.requestURI } returns "/test-uri"
                every { request.getHeader(any()) } returns ""

                val response = exceptionHandler.methodArgumentNotValidException(exception, request)

                response.data shouldBe null
            }

            it("should handle MismatchedInputException") {
                val exception = ServerWebInputException("")

                every { request.requestURI } returns "/test-uri"
                every { request.getHeader(any()) } returns ""

                val response = exceptionHandler.methodArgumentNotValidException(exception, request)

                response.data shouldBe null
            }

            it("should handle other exceptions") {
                val exception = ServerWebInputException("")

                every { request.requestURI } returns "/test-uri"
                every { request.getHeader(any()) } returns ""

                val response = exceptionHandler.methodArgumentNotValidException(exception, request)

                response.data shouldBe null
            }
        }

        describe("handleSignatureException") {
            it("should handle SignatureException") {
                val exception = SignatureException("Signature error")

                every { request.requestURI } returns "/test-uri"
                every { request.getHeader(any()) } returns ""

                val response = exceptionHandler.handleSignatureException(exception, request)

                response.data shouldBe "Signature error"
            }
        }

        describe("exception") {
            it("should handle generic exceptions") {
                val exception = Exception("Generic error")

                val response = exceptionHandler.exception(exception)

                response.data shouldBe "Generic error"
            }
        }
    }
})

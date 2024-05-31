package com.restaurant.be.common.exception

import club.minnced.discord.webhook.WebhookClient
import com.fasterxml.jackson.core.JsonLocation
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.restaurant.be.common.response.CommonResponse
import com.restaurant.be.common.response.ErrorCode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.validation.FieldError
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ServerWebInputException
import java.security.SignatureException
import javax.servlet.http.HttpServletRequest
import kotlin.reflect.KParameter

class GlobalExceptionHandlerTest : DescribeSpec({

    val webhookClient = mockk<WebhookClient>(relaxed = true)
    val request = mockk<HttpServletRequest>()
    val exceptionHandler = GlobalExceptionHandler(webhookClient)

    describe("GlobalExceptionHandler") {

        describe("methodArgumentNotValidException for ServerWebInputException") {
            it("should handle MissingKotlinParameterException") {
                val parameter: KParameter = mockk {
                    every { name } returns "testParam"
                }
                val message = "Missing required parameter"
                val missingKotlinParameterException =
                    MissingKotlinParameterException(parameter, null, message)
                val exception =
                    ServerWebInputException("Bad request", null, missingKotlinParameterException)

                every { request.requestURI } returns "/test-uri"
                every { request.getHeader(any()) } returns ""

                val response: CommonResponse<String?> =
                    exceptionHandler.methodArgumentNotValidException(exception, request)

                response.data shouldBe "항목 testParam을 확인해주세요"
            }

            it("should handle MismatchedInputException") {
                val message = "Mismatched input"
                val parser: JsonParser = mockk()
                val location: JsonLocation = mockk()

                every { parser.tokenLocation } returns location
                every { location.lineNr } returns 1
                every { location.columnNr } returns 1

                val mismatchedInputException = MismatchedInputException.from(parser, message)
                val exception =
                    ServerWebInputException("Bad request", null, mismatchedInputException)

                every { request.requestURI } returns "/test-uri"
                every { request.getHeader(any()) } returns ""

                val response = exceptionHandler.methodArgumentNotValidException(exception, request)

                response.data shouldBe message
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

        describe("methodArgumentNotValidException") {

            it("should handle WebExchangeBindException and return error response") {
                val fieldError: FieldError = mockk {
                    every { field } returns "testField"
                    every { defaultMessage } returns "Invalid value"
                    every { rejectedValue } returns "invalidValue"
                }
                val webExchangeBindException: WebExchangeBindException = mockk {
                    every { allErrors } returns listOf(fieldError)
                }

                every { request.getHeader(any()) } returns ""
                every { request.requestURI } returns "/test-uri"

                val response: CommonResponse<String> =
                    exceptionHandler.methodArgumentNotValidException(
                        webExchangeBindException,
                        request
                    )

                response.data shouldBe "[Error(field=testField, message=Invalid value, value=invalidValue)]"
                response.errorCode shouldBe ErrorCode.COMMON_INVALID_PARAMETER.toString()
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

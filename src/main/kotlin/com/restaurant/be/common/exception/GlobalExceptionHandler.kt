package com.restaurant.be.common.exception

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.send.WebhookEmbed
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.restaurant.be.common.response.CommonResponse
import com.restaurant.be.common.response.Error
import com.restaurant.be.common.response.ErrorCode
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ServerWebInputException
import java.io.BufferedReader
import java.security.SignatureException
import javax.servlet.http.HttpServletRequest

@RestControllerAdvice
class GlobalExceptionHandler(
    @Qualifier("MonitorWebhook") private val webhookClient: WebhookClient
) {

    @ExceptionHandler(value = [ServerException::class])
    fun handleServerException(
        ex: ServerException,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val embed = WebhookEmbed(
            null,
            null,
            "code: " + ex.code + "\n" + ex.javaClass.simpleName,
            null,
            null,
            null,
            WebhookEmbed.EmbedTitle(request.requestURI, null),
            null,
            emptyList()
        )
        val builder = WebhookMessageBuilder()
        builder.setContent(ex.message)
        builder.addEmbeds(embed)
        builder.addEmbeds(parseBodyToEmbed(request))
        builder.addEmbeds(parseUserAgentToEmbed(request))
        webhookClient.send(builder.build())

        val response = CommonResponse.fail(ex.message, ex.javaClass.simpleName)
        return ResponseEntity(response, null, ex.code)
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = [ServerWebInputException::class])
    fun methodArgumentNotValidException(
        e: ServerWebInputException,
        request: HttpServletRequest
    ): CommonResponse<String?> {
        val data = if (e.cause?.cause is MissingKotlinParameterException) {
            val param = (e.cause?.cause as MissingKotlinParameterException).parameter.name
            "항목 ${param}을 확인해주세요"
        } else if (e.cause?.cause is MismatchedInputException) {
            e.message
        } else {
            null
        }

        val embed = WebhookEmbed(
            null,
            null,
            data,
            null,
            null,
            null,
            WebhookEmbed.EmbedTitle(request.requestURI, null),
            null,
            emptyList()
        )
        val builder = WebhookMessageBuilder()
        builder.setContent(e.message)
        builder.addEmbeds(embed)
        builder.addEmbeds(parseBodyToEmbed(request))
        builder.addEmbeds(parseUserAgentToEmbed(request))
        webhookClient.send(builder.build())

        val errorResponse = CommonResponse.fail(data, ErrorCode.COMMON_NULL_PARAMETER)
        return errorResponse
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = [WebExchangeBindException::class, MethodArgumentNotValidException::class])
    fun methodArgumentNotValidException(
        e: WebExchangeBindException,
        request: HttpServletRequest
    ): CommonResponse<String> {
        val errors = mutableListOf<Error>()
        e.allErrors.forEach {
            val error = Error(
                field = (it as FieldError).field,
                message = it.defaultMessage,
                value = it.rejectedValue
            )
            errors.add(error)
        }

        val embed = WebhookEmbed(
            null,
            null,
            errors.toString(),
            null,
            null,
            null,
            WebhookEmbed.EmbedTitle(request.requestURI, null),
            null,
            emptyList()
        )
        val builder = WebhookMessageBuilder()
        builder.setContent(ErrorCode.COMMON_INVALID_PARAMETER.errorMsg)
        builder.addEmbeds(embed)
        builder.addEmbeds(parseBodyToEmbed(request))
        builder.addEmbeds(parseUserAgentToEmbed(request))
        webhookClient.send(builder.build())

        val errorResponse =
            CommonResponse.fail(errors.toString(), ErrorCode.COMMON_INVALID_PARAMETER)

        return errorResponse
    }

    @ExceptionHandler(SignatureException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleSignatureException(
        e: SignatureException,
        request: HttpServletRequest
    ): CommonResponse<String?> {
        val embed = WebhookEmbed(
            null,
            null,
            e.message + "/" + e::class.java.simpleName,
            null,
            null,
            null,
            WebhookEmbed.EmbedTitle(request.requestURI, null),
            null,
            emptyList()
        )
        val builder = WebhookMessageBuilder()
        builder.setContent("서버에서 정의하지 않은 서버입니다.")
        builder.addEmbeds(embed)
        builder.addEmbeds(parseBodyToEmbed(request))
        builder.addEmbeds(parseUserAgentToEmbed(request))
        webhookClient.send(builder.build())

        val errorResponse = CommonResponse.fail(e.message, e::class.java.simpleName)
        return errorResponse
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = [Exception::class])
    fun exception(
        e: Exception
    ): CommonResponse<String?> {
        val errorResponse = CommonResponse.fail(e.message, e::class.java.simpleName)
        return errorResponse
    }

    private fun parseBodyToEmbed(request: HttpServletRequest): WebhookEmbed {
        return WebhookEmbed(
            null,
            null,
            parseBody(request),
            null,
            null,
            null,
            WebhookEmbed.EmbedTitle("Body", null),
            null,
            emptyList()
        )
    }

    private fun parseBody(request: HttpServletRequest): String {
        return runBlocking {
            val body = runCatching {
                request.inputStream.bufferedReader().use(BufferedReader::readText)
            }.getOrDefault("")
            body
        }
    }

    private fun parseUserAgentToEmbed(request: HttpServletRequest): WebhookEmbed {
        val userAgent = request.getHeader("User-Agent") ?: "Unknown"
        return WebhookEmbed(
            null,
            null,
            userAgent,
            null,
            null,
            null,
            WebhookEmbed.EmbedTitle("User Agent", null),
            null,
            emptyList()
        )
    }
}

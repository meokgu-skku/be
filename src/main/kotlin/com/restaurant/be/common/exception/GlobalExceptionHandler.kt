package com.restaurant.be.common.exception

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.restaurant.be.common.response.CommonResponse
import com.restaurant.be.common.response.Error
import com.restaurant.be.common.response.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.ServerWebInputException
import java.security.SignatureException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ServerException::class)
    fun handleServerException(
        ex: ServerException,
        exchange: ServerWebExchange
    ): ResponseEntity<Any> {
        val response = CommonResponse.fail(ex.message, ex.javaClass.simpleName)
        return ResponseEntity(response, null, ex.code)
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = [ServerWebInputException::class])
    fun methodArgumentNotValidException(
        e: ServerWebInputException,
        exchange: ServerWebExchange
    ): CommonResponse<String?> {
        val data =
            if (e.cause?.cause is MissingKotlinParameterException) {
                val param = (e.cause?.cause as MissingKotlinParameterException).parameter.name
                "항목 ${param}을 확인해주세요"
            } else if (e.cause?.cause is MismatchedInputException) {
                e.message
            } else {
                null
            }

        val errorResponse = CommonResponse.fail(data, ErrorCode.COMMON_NULL_PARAMETER)
        return errorResponse
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = [WebExchangeBindException::class])
    fun methodArgumentNotValidException(
        e: WebExchangeBindException,
        exchange: ServerWebExchange
    ): CommonResponse<String> {
        val errors = mutableListOf<Error>()
        e.allErrors.forEach {
            val error =
                Error(
                    field = (it as FieldError).field,
                    message = it.defaultMessage,
                    value = it.rejectedValue
                )
            errors.add(error)
        }

        val errorResponse =
            CommonResponse
                .fail(errors.toString(), ErrorCode.COMMON_INVALID_PARAMETER)

        return errorResponse
    }

    @ExceptionHandler(SignatureException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleSignatureException(
        e: SignatureException
    ): CommonResponse<String?> {
        val errorResponse = CommonResponse.fail(e.message, e::class.java.simpleName)
        return errorResponse
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = [Exception::class])
    fun exception(
        e: Exception,
        exchange: ServerWebExchange
    ): CommonResponse<String?> {
        val errorResponse = CommonResponse.fail(e.message, e::class.java.simpleName)
        return errorResponse
    }
}

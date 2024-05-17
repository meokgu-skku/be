package com.restaurant.be.user.domain.service

import com.restaurant.be.common.exception.InvalidEmailCodeException
import com.restaurant.be.common.exception.SendEmailException
import com.restaurant.be.common.exception.SkkuEmailException
import com.restaurant.be.common.redis.RedisRepository
import com.restaurant.be.user.presentation.dto.SendEmailRequest
import com.restaurant.be.user.presentation.dto.ValidateEmailRequest
import com.restaurant.be.user.presentation.dto.ValidateEmailResponse
import com.restaurant.be.user.presentation.dto.common.EmailSendType
import com.restaurant.be.user.repository.EmailRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.TimeUnit

@Service
class ValidateEmailService(
    private val emailRepository: EmailRepository,
    @Value("\${aws.sender-email}") private val emailSource: String,
    private val redisRepository: RedisRepository
) {
    private val signUpTemplate =
        EmailRepository::class.java.classLoader
            .getResource("sign-up-template.html")!!
            .readText()
    private val resetPasswordTemplate =
        EmailRepository::class.java.classLoader
            .getResource("reset-password-template.html")!!
            .readText()

    fun sendValidateCode(request: SendEmailRequest) {
        if (request.email.split("@")[1] != "g.skku.edu" ||
            request.email.split("@")[1] != "skku.edu"
        ) {
            throw SkkuEmailException()
        }
        try {
            val code = emailRepository.generateRandomCode()
            val email = request.email
            if (request.sendType == EmailSendType.SIGN_UP) {
                val message = software.amazon.awssdk.services.ses.model.SendEmailRequest
                    .builder()
                    .source(emailSource)
                    .destination {
                        it.toAddresses(email)
                    }
                    .message {
                        it.subject {
                            it.data("먹꾸스꾸 회원가입 인증번호입니다.")
                        }
                        it.body {
                            it.html {
                                it.data(signUpTemplate.replace("AUTHENTICATION_CODE", code))
                            }
                        }
                    }.build()

                redisRepository.setValue(
                    "user:$email:${request.sendType.name.lowercase(Locale.getDefault())}_code",
                    code,
                    3,
                    TimeUnit.MINUTES
                )
                emailRepository.sendEmail(message)
            } else {
                val message = software.amazon.awssdk.services.ses.model.SendEmailRequest
                    .builder()
                    .source(emailSource)
                    .destination {
                        it.toAddresses(email)
                    }
                    .message {
                        it.subject {
                            it.data("먹꾸스꾸 비밀번호 재설정 코드입니다.")
                        }
                        it.body {
                            it.html {
                                it.data(resetPasswordTemplate.replace("AUTHENTICATION_CODE", code))
                            }
                        }
                    }.build()

                redisRepository.setValue(
                    "user:$email:${request.sendType.name.lowercase(Locale.getDefault())}_code",
                    code,
                    3,
                    TimeUnit.MINUTES
                )
                emailRepository.sendEmail(message)
            }
        } catch (e: Exception) {
            throw SendEmailException()
        }
    }

    fun validateEmail(request: ValidateEmailRequest): ValidateEmailResponse {
        if (!isValidCode(request.email, request.code, request.sendType)) {
            throw InvalidEmailCodeException()
        }

        val token = (UUID.randomUUID().toString() + UUID.randomUUID().toString()).replace("-", "")
        redisRepository.setValue(
            "user:${request.email}:${request.sendType.name.lowercase(Locale.getDefault())}_token",
            token,
            3,
            TimeUnit.MINUTES
        )

        return ValidateEmailResponse(token)
    }

    private fun isValidCode(email: String, code: String, sendType: EmailSendType): Boolean {
        val key = "user:$email:${sendType.name.lowercase(Locale.getDefault())}_code"
        return redisRepository.getValue(key) == code
    }
}

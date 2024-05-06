package com.restaurant.be.user.domain.service

import com.restaurant.be.common.exception.SendEmailException
import com.restaurant.be.common.exception.SkkuEmailException
import com.restaurant.be.common.redis.RedisRepository
import com.restaurant.be.user.presentation.dto.SendEmailRequest
import com.restaurant.be.user.presentation.dto.common.EmailSendType
import com.restaurant.be.user.repository.EmailRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class ValidateEmailService(
    private val emailRepository: EmailRepository,
    @Value("\${aws.sender-email}") private val emailSource: String,
    private val redisRepository: RedisRepository
) {
    private val emailTemplate =
        EmailRepository::class.java.classLoader
            .getResource("email-template.html")!!
            .readText()
    private val passwordTemplate =
        EmailRepository::class.java.classLoader
            .getResource("password-template.html")!!
            .readText()

    fun sendValidateCode(request: SendEmailRequest) {
        if (request.email.split("@")[1] != "g.skku.edu") {
            throw SkkuEmailException()
        }

        try {
            val code = emailRepository.generateRandomCode()
            val email = request.email
            if (request.sendType == EmailSendType.EMAIL_VALIDATION) {
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
                                it.data(emailTemplate.replace("AUTHENTICATION_CODE", code))
                            }
                        }
                    }.build()

                redisRepository.setValue("user:$email:emailCode", code, 3, TimeUnit.MINUTES)
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
                                it.data(passwordTemplate.replace("AUTHENTICATION_CODE", code))
                            }
                        }
                    }.build()

                redisRepository.setValue("user:$email:passwordCode", code, 3, TimeUnit.MINUTES)
                emailRepository.sendEmail(message)
            }
        } catch (e: Exception) {
            throw SendEmailException()
        }
    }
}

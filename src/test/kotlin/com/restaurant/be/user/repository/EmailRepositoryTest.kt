package com.restaurant.be.user.repository

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import software.amazon.awssdk.services.ses.SesClient
import software.amazon.awssdk.services.ses.model.SendEmailRequest
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.random.Random

class EmailRepositoryTest : DescribeSpec({

    val sesClient = mockk<SesClient>()
    val emailRepository = EmailRepository(sesClient)

    describe("EmailRepository") {

        context("sendEmail") {
            it("should call sesClient.sendEmail with the correct request") {
                val sendEmailRequest = SendEmailRequest.builder()
                    .destination { it.toAddresses("test@example.com") }
                    .message {
                        it.subject { it.data("Test Email") }
                        it.body { it.text { it.data("This is a test email.") } }
                    }
                    .source("noreply@example.com")
                    .build()

                every { sesClient.sendEmail(any<SendEmailRequest>()) } returns mockk()

                emailRepository.sendEmail(sendEmailRequest)

                coVerify { sesClient.sendEmail(sendEmailRequest) }
            }
        }

        context("generateRandomCode") {
            it("should generate a 6 digit random code") {
                val fixedSeed = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
                val rnd = Random(fixedSeed)

                emailRepository.generateRandomCode().length shouldBe 6
            }
        }
    }
})

package com.restaurant.be.user.repository

import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Repository
import software.amazon.awssdk.services.ses.SesClient
import software.amazon.awssdk.services.ses.model.SendEmailRequest
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.random.Random

@Repository
class EmailRepository(
    private val sesClient: SesClient

) {
    private val rnd = Random(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))

    fun sendEmail(sendEmailRequest: SendEmailRequest) {
        runBlocking {
            sesClient.sendEmail(sendEmailRequest)
        }
    }

    fun generateRandomCode(): String {
        return String.format("%06d", rnd.nextInt(0, 1000000))
    }
}

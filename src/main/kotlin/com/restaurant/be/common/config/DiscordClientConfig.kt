package com.restaurant.be.common.config

import club.minnced.discord.webhook.WebhookClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DiscordClientConfig(
    @Value("\${discord.monitor}") val monitorUrl: String
) {
    private val dummyUrl =
        "https://discord.com/api/webhooks/99999990000000/213aWRR5sEeY5UhOk7twvFSDFVC-Feqw"

    @Bean("MonitorWebhook")
    fun monitorReportClient(): WebhookClient {
        return if (monitorUrl.startsWith("https://discord.com")) {
            WebhookClient.withUrl(monitorUrl)
        } else {
            WebhookClient.withUrl(dummyUrl)
        }
    }
}

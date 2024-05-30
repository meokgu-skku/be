package com.restaurant.be.common.config

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.test.context.ContextConfiguration

@ContextConfiguration(classes = [DiscordClientConfig::class])
class DiscordClientConfigTest : DescribeSpec({

    describe("DiscordClientConfig") {
        val validMonitorUrl = "https://discord.com/api/v9/webhooks/123456789012345678/abcdefg12345"
        val invalidMonitorUrl = "http://invalid.url"
        val dummyUrl = "https://discord.com/api/v9/webhooks/99999990000000/213aWRR5sEeY5UhOk7twvFSDFVC-Feqw"

        describe("monitorReportClient") {
            it("should return WebhookClient with monitorUrl when it is valid") {
                // Given
                val config = DiscordClientConfig(validMonitorUrl)

                // When
                val webhookClient = config.monitorReportClient()

                // Then
                webhookClient.url shouldBe validMonitorUrl
            }

            it("should return WebhookClient with dummyUrl when monitorUrl is invalid") {
                // Given
                val config = DiscordClientConfig(invalidMonitorUrl)

                // When
                val webhookClient = config.monitorReportClient()

                // Then
                webhookClient.url shouldBe dummyUrl
            }
        }
    }
})

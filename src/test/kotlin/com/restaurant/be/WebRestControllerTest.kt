package com.restaurant.be

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.core.env.Environment

class WebRestControllerTest : DescribeSpec({

    val environment = mockk<Environment>()
    val controller = WebRestController(environment)

    describe("WebRestController") {

        context("when there is an active profile") {
            it("should return the active profile") {
                val activeProfile = "test-profile"
                every { environment.activeProfiles } returns arrayOf(activeProfile)

                val result = controller.getProfile()

                result shouldBe activeProfile
            }
        }

        context("when there is no active profile") {
            it("should return 'No Active Profile'") {
                every { environment.activeProfiles } returns arrayOf()

                val result = controller.getProfile()

                result shouldBe "No Active Profile"
            }
        }
    }
})

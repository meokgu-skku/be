package com.restaurant.be

import com.restaurant.be.common.CustomDescribeSpec
import com.restaurant.be.common.IntegrationTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.context.ApplicationContextException

@IntegrationTest
class BeApplicationTest(
    context: org.springframework.context.ApplicationContext
) : CustomDescribeSpec() {

    init {
        describe("BeApplication") {
            it("should load Spring context successfully") {
                context shouldNotBe null
            }

            it("should have BeApplication bean") {
                context.containsBean("beApplication") shouldBe true
            }

            it("test") {
                shouldThrow<ApplicationContextException> {
                    main(arrayOf())
                }
            }
        }
    }
}

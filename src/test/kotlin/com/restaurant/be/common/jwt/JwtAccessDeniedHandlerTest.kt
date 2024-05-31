package com.restaurant.be.common.jwt

import io.kotest.core.spec.style.DescribeSpec
import io.mockk.mockk
import io.mockk.verify
import org.springframework.security.access.AccessDeniedException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JwtAccessDeniedHandlerTest : DescribeSpec({

    describe("JwtAccessDeniedHandler") {

        val handler = JwtAccessDeniedHandler()

        it("should set HTTP response status to SC_FORBIDDEN") {
            // Given
            val request = mockk<HttpServletRequest>()
            val response = mockk<HttpServletResponse>(relaxed = true)
            val accessDeniedException = mockk<AccessDeniedException>()

            // When
            handler.handle(request, response, accessDeniedException)

            // Then
            verify { response.sendError(HttpServletResponse.SC_FORBIDDEN) }
        }
    }
})

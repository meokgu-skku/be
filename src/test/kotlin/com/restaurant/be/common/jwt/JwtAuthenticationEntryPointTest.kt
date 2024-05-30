package com.restaurant.be.common.jwt

import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.security.core.AuthenticationException
import org.springframework.web.servlet.HandlerExceptionResolver
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JwtAuthenticationEntryPointTest : DescribeSpec({

    describe("JwtAuthenticationEntryPoint") {

        val resolver = mockk<HandlerExceptionResolver>()
        val entryPoint = JwtAuthenticationEntryPoint(resolver)

        it("should resolve exception using HandlerExceptionResolver") {
            // Given
            val request = mockk<HttpServletRequest>()
            val response = mockk<HttpServletResponse>(relaxed = true)
            val authException = mockk<AuthenticationException>()
            val exception = Exception("Test Exception")

            every { request.getAttribute("exception") } returns exception
            every { resolver.resolveException(request, response, null, exception) } returns null

            // When
            entryPoint.commence(request, response, authException)

            // Then
            verify { resolver.resolveException(request, response, null, exception) }
        }
    }
})

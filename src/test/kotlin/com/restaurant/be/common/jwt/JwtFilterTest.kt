package com.restaurant.be.common.jwt

import com.restaurant.be.common.response.Token
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JwtFilterTest : DescribeSpec({

    val tokenProvider = mockk<TokenProvider>()
    val jwtUserRepository = mockk<JwtUserRepository>()
    val request = mockk<HttpServletRequest>(relaxed = true)
    val response = mockk<HttpServletResponse>(relaxed = true)
    val filterChain = mockk<FilterChain>(relaxed = true)
    val jwtFilter = JwtFilter(tokenProvider, jwtUserRepository)

    beforeEach {
        clearAllMocks()
        val securityContext = mockk<SecurityContext>()
        SecurityContextHolder.setContext(securityContext)
    }

    describe("doFilterInternal") {

        it("should reissue token when refresh token is present") {
            val refreshToken = "validRefreshToken"
            val accessToken = "oldAccessToken"
            every { request.getHeader(JwtFilter.AUTHORIZATION_HEADER) } returns accessToken
            every { request.getHeader(JwtFilter.REFRESH_TOKEN_HEADER) } returns refreshToken
            every { tokenProvider.resolveToken(accessToken) } returns accessToken
            every { tokenProvider.resolveToken(refreshToken) } returns refreshToken
            every { tokenProvider.validateToken(refreshToken) } returns true
            every { tokenProvider.tokenReissue(accessToken, refreshToken) } returns Token(
                "newAccessToken",
                "newRefreshToken",
                "Bearer",
                1000,
                Date.from(Date().toInstant())
            )
            every { response.getHeader(JwtFilter.AUTHORIZATION_HEADER) } returns null

            jwtFilter.doFilterInternal(request, response, filterChain)

            verify { response.addHeader(JwtFilter.AUTHORIZATION_HEADER, "newAccessToken") }
            verify { filterChain.doFilter(request, response) }
        }
    }
})

package com.restaurant.be.common.jwt

import com.restaurant.be.common.jwt.JwtFilter.Companion.AUTHORIZATION_HEADER
import com.restaurant.be.common.jwt.JwtFilter.Companion.REFRESH_TOKEN_HEADER
import com.restaurant.be.common.response.Token
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.springframework.security.core.Authentication
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

        it("when refresh token is null should validate access token") {
            val refreshToken = null
            val accessToken = "validAccessToken"

            every { request.getHeader(JwtFilter.AUTHORIZATION_HEADER) } returns accessToken
            every { request.getHeader(JwtFilter.REFRESH_TOKEN_HEADER) } returns refreshToken
            every { tokenProvider.resolveToken(accessToken) } returns accessToken
            every { tokenProvider.resolveToken(refreshToken) } returns refreshToken
            every { tokenProvider.validateToken(accessToken) } returns true
            every { tokenProvider.getEmailFromToken(accessToken) } returns "test@test.com"
            every { jwtUserRepository.validTokenByEmail(any()) } returns true
            val authentication = mockk<Authentication>()
            every { tokenProvider.getAuthentication(accessToken) } returns authentication

            val securityContext = SecurityContextHolder.getContext()
            every { securityContext.authentication = authentication } just Runs

            jwtFilter.doFilterInternal(request, response, filterChain)

            verify { filterChain.doFilter(request, response) }
            verify { securityContext.authentication = authentication }
        }

        it("when access token is null should return") {
            val refreshToken = "validRefreshToken"
            val accessToken = null

            every { request.getHeader(JwtFilter.AUTHORIZATION_HEADER) } returns accessToken
            every { request.getHeader(JwtFilter.REFRESH_TOKEN_HEADER) } returns refreshToken
            every { tokenProvider.resolveToken(accessToken) } returns accessToken
            every { tokenProvider.resolveToken(refreshToken) } returns refreshToken
            every { tokenProvider.validateToken(refreshToken) } returns true

            jwtFilter.doFilterInternal(request, response, filterChain)

            verify(exactly = 0) { filterChain.doFilter(request, response) }
        }

        it("when refreshToken is null and invalid accessToken should signatureException") {
            val refreshToken = null
            val accessToken = "invalidAccessToken"

            every { request.getHeader(AUTHORIZATION_HEADER) } returns accessToken
            every { request.getHeader(REFRESH_TOKEN_HEADER) } returns refreshToken
            every { tokenProvider.resolveToken(accessToken) } returns accessToken
            every { tokenProvider.resolveToken(refreshToken) } returns refreshToken
            every { tokenProvider.validateToken(accessToken) } returns false
        }

        it("when accessToken is null should return and not call doFilter") {
            val refreshToken = "validRefreshToken"
            val accessToken = null

            every { request.getHeader(JwtFilter.AUTHORIZATION_HEADER) } returns accessToken
            every { request.getHeader(JwtFilter.REFRESH_TOKEN_HEADER) } returns refreshToken
            every { tokenProvider.resolveToken(accessToken) } returns accessToken
            every { tokenProvider.resolveToken(refreshToken) } returns refreshToken
            every { tokenProvider.validateToken(refreshToken) } returns true

            jwtFilter.doFilterInternal(request, response, filterChain)

            verify(exactly = 0) { filterChain.doFilter(request, response) }
        }
    }
})

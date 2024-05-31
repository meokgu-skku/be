package com.restaurant.be.common.jwt

import com.restaurant.be.common.exception.InvalidTokenException
import com.restaurant.be.common.redis.RedisRepository
import io.jsonwebtoken.Jwts
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import java.util.*

class TokenProviderTest : DescribeSpec({

    val secret = "testsecretkeytestsecretkeytestsecretkeytestsecretkeytestsecretkey"
    val redisRepository = mockk<RedisRepository>()
    lateinit var tokenProvider: TokenProvider

    beforeEach {
        tokenProvider = TokenProvider(
            secret = secret,
            tokenValidityInMilliseconds = 3600,
            accessTokenValidityInSeconds = 1800,
            refreshTokenValidityInSeconds = 7200,
            redisRepository = redisRepository
        )
        tokenProvider.afterPropertiesSet()
    }

    describe("TokenProvider") {

        describe("getAllClaimsFromToken") {
            it("should return claims from a valid token") {
                // Given
                val email = "test@example.com"
                val token = tokenProvider.createTokens(email, listOf("ROLE_USER")).accessToken

                // When
                val claims = tokenProvider.getAllClaimsFromToken(token)

                // Then
                claims.subject shouldBe email
            }
        }

        describe("getEmailFromToken") {
            it("should return email from token") {
                // Given
                val email = "test@example.com"
                val token = tokenProvider.createTokens(email, listOf("ROLE_USER")).accessToken

                // When
                val extractedEmail = tokenProvider.getEmailFromToken(token)

                // Then
                extractedEmail shouldBe email
            }
        }

        describe("getRolesFromToken") {
            it("should return roles from token") {
                // Given
                val email = "test@example.com"
                val roles = "ROLE_USER,ROLE_ADMIN"
                val token = tokenProvider.createAccessToken(email, roles)

                // When
                val extractedRoles = tokenProvider.getRolesFromToken(token)

                // Then
                extractedRoles shouldBe roles
            }
        }

        describe("getAuthentication") {
            it("should return authentication from token") {
                // Given
                val email = "test@example.com"
                val token = tokenProvider.createTokens(email, listOf("ROLE_USER")).accessToken

                // When
                val authentication = tokenProvider.getAuthentication(token)

                // Then
                authentication shouldNotBe null
                authentication.principal shouldNotBe null
            }
        }

        describe("resolveToken") {
            it("should return null for null token") {
                // When
                val token = tokenProvider.resolveToken(null)

                // Then
                token shouldBe null
            }

            it("should return null for empty token") {
                // When
                val token = tokenProvider.resolveToken("")

                // Then
                token shouldBe null
            }

            it("should return null for non-Bearer token") {
                // When
                val token = tokenProvider.resolveToken("InvalidToken")

                // Then
                token shouldBe null
            }

            it("should return token for valid Bearer token") {
                // Given
                val token = "Bearer validToken"

                // When
                val resolvedToken = tokenProvider.resolveToken(token)

                // Then
                resolvedToken shouldBe "validToken"
            }
        }

        describe("tokenReissue") {
            it("should reissue a new access token if refresh token is valid") {
                // Given
                val email = "test@example.com"
                val roles = listOf("ROLE_USER")
                val tokens = tokenProvider.createTokens(email, roles)
                val accessToken = tokens.accessToken
                val refreshToken = tokens.refreshToken

                every { redisRepository.getValue("RT:$email") } returns refreshToken

                // When
                val newTokens = tokenProvider.tokenReissue(accessToken, refreshToken)

                // Then
                newTokens shouldNotBe null
                newTokens.accessToken shouldNotBe null
                newTokens.refreshToken shouldNotBe null
            }

            it("should throw InvalidTokenException if refresh token is invalid") {
                // Given
                val email = "test@example.com"
                val roles = listOf("ROLE_USER")
                val tokens = tokenProvider.createTokens(email, roles)
                val accessToken = tokens.accessToken
                val refreshToken = "invalidRefreshToken"

                every { redisRepository.getValue("RT:$email") } returns refreshToken

                // When / Then
                shouldThrow<InvalidTokenException> {
                    tokenProvider.tokenReissue(accessToken, "test")
                }
            }
        }

        describe("validateToken") {
            it("should return true for a valid token") {
                // Given
                val token = tokenProvider.createTokens("test@example.com", listOf("ROLE_USER")).accessToken

                // When
                val isValid = tokenProvider.validateToken(token)

                // Then
                isValid shouldBe true
            }

            it("should log and return false for a token with invalid signature") {
                // Given
                val token = tokenProvider.createTokens("test@example.com", listOf("ROLE_USER")).accessToken
                val invalidToken = token + "invalid"
                mockkObject(tokenProvider.log)
                every { tokenProvider.log.info("잘못된 JWT 서명입니다.") } returns Unit

                // When
                val isValid = tokenProvider.validateToken(invalidToken)

                // Then
                isValid shouldBe false
                verify { tokenProvider.log.info("잘못된 JWT 서명입니다.") }
                unmockkObject(tokenProvider.log)
            }

            it("should log and return false for a malformed token") {
                // Given
                val malformedToken = "malformed.token"
                mockkObject(tokenProvider.log)
                every { tokenProvider.log.info("잘못된 JWT 서명입니다.") } returns Unit

                // When
                val isValid = tokenProvider.validateToken(malformedToken)

                // Then
                isValid shouldBe false
                verify { tokenProvider.log.info("잘못된 JWT 서명입니다.") }
                unmockkObject(tokenProvider.log)
            }

            it("should log and return false for an expired token") {
                // Given
                val expiredToken = Jwts.builder()
                    .setSubject("test@example.com")
                    .setExpiration(Date(System.currentTimeMillis() - 1000))
                    .signWith(tokenProvider.key)
                    .compact()
                mockkObject(tokenProvider.log)
                every { tokenProvider.log.info("만료된 JWT 토큰입니다.") } returns Unit

                // When
                val isValid = tokenProvider.validateToken(expiredToken)

                // Then
                isValid shouldBe false
                verify { tokenProvider.log.info("만료된 JWT 토큰입니다.") }
                unmockkObject(tokenProvider.log)
            }

            it("should log and return false for an unsupported token") {
                // Given
                val unsupportedToken = Jwts.builder()
                    .setPayload("unsupportedPayload")
                    .signWith(tokenProvider.key)
                    .compact()
                mockkObject(tokenProvider.log)
                every { tokenProvider.log.info("지원되지 않는 JWT 토큰입니다.") } returns Unit

                // When
                val isValid = tokenProvider.validateToken(unsupportedToken)

                // Then
                isValid shouldBe false
                verify { tokenProvider.log.info("지원되지 않는 JWT 토큰입니다.") }
                unmockkObject(tokenProvider.log)
            }

            it("should log and return false for an illegal argument token") {
                // Given
                val illegalArgumentToken = ""
                mockkObject(tokenProvider.log)
                every { tokenProvider.log.info("JWT 토큰이 잘못되었습니다.") } returns Unit

                // When
                val isValid = tokenProvider.validateToken(illegalArgumentToken)

                // Then
                isValid shouldBe false
                verify { tokenProvider.log.info("JWT 토큰이 잘못되었습니다.") }
                unmockkObject(tokenProvider.log)
            }
        }

        describe("createAccessToken") {
            it("should create a valid access token") {
                // Given
                val email = "test@example.com"
                val roles = "ROLE_USER"

                // When
                val token = tokenProvider.createAccessToken(email, roles)

                // Then
                token shouldNotBe null
                val claims = tokenProvider.getAllClaimsFromToken(token)
                claims.subject shouldBe email
            }
        }
    }
})

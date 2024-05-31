package com.restaurant.be.user.domain.service

import com.restaurant.be.common.exception.InvalidPasswordException
import com.restaurant.be.common.exception.NotFoundUserEmailException
import com.restaurant.be.common.jwt.TokenProvider
import com.restaurant.be.common.password.PasswordService
import com.restaurant.be.common.redis.RedisRepository
import com.restaurant.be.user.presentation.dto.SignInUserRequest
import com.restaurant.be.user.presentation.dto.SignInUserResponse
import com.restaurant.be.user.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import javax.transaction.Transactional

@Service
class SignInUserService(
    private val userRepository: UserRepository,
    private val tokenProvider: TokenProvider,
    private val redisRepository: RedisRepository
) {

    @Transactional
    fun signInUser(request: SignInUserRequest): SignInUserResponse {
        with(request) {
            val user = userRepository.findByEmail(email) ?: throw NotFoundUserEmailException()
            val isValid = user.password.isNotEmpty() &&
                PasswordService.isValidPassword(this.password, user.password)
            if (!isValid) {
                throw InvalidPasswordException()
            }

            val token = tokenProvider.createTokens(email, user.roles)

            redisRepository.setValue(
                "RT:$email",
                token.refreshToken,
                tokenProvider.refreshTokenValidityInMilliseconds,
                TimeUnit.MILLISECONDS
            )
            return SignInUserResponse(user, token)
        }
    }
}

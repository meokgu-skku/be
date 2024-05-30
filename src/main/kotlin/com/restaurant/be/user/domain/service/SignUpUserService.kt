package com.restaurant.be.user.domain.service

import com.restaurant.be.common.exception.DuplicateUserEmailException
import com.restaurant.be.common.exception.DuplicateUserNicknameException
import com.restaurant.be.common.jwt.TokenProvider
import com.restaurant.be.common.redis.RedisRepository
import com.restaurant.be.user.presentation.dto.SignUpUserRequest
import com.restaurant.be.user.presentation.dto.SignUpUserResponse
import com.restaurant.be.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

@Service
class SignUpUserService(
    private val userRepository: UserRepository,
    private val tokenProvider: TokenProvider,
    private val redisRepository: RedisRepository
) {

    @Transactional
    fun signUpUser(request: SignUpUserRequest): SignUpUserResponse {
        with(request) {
            val user = userRepository.findByNicknameOrEmail(nickname, email)
            if (user?.nickname == nickname) {
                throw DuplicateUserNicknameException()
            }

            if (user?.email == email) {
                throw DuplicateUserEmailException()
            }

            val newUser = userRepository.save(toEntity())

            val token = tokenProvider.createTokens(email, newUser.roles)

            redisRepository.setValue(
                redisRepository.REFRESH_PREFIX + email,
                token.refreshToken,
                tokenProvider.refreshTokenValidityInMilliseconds,
                TimeUnit.MILLISECONDS
            )
            return SignUpUserResponse(user = newUser, token = token)
        }
    }
}

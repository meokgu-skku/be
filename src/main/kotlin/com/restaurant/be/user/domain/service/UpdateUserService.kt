package com.restaurant.be.user.domain.service

import com.restaurant.be.common.exception.InvalidUserResetPasswordStateException
import com.restaurant.be.common.exception.NotEqualTokenException
import com.restaurant.be.common.exception.NotFoundUserEmailException
import com.restaurant.be.common.redis.RedisRepository
import com.restaurant.be.user.presentation.dto.UpdatePasswordRequest
import com.restaurant.be.user.presentation.dto.UpdateUserRequest
import com.restaurant.be.user.presentation.dto.UpdateUserResponse
import com.restaurant.be.user.presentation.dto.common.EmailSendType
import com.restaurant.be.user.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

@Service
class UpdateUserService(
    private val redisRepository: RedisRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun updatePassword(request: UpdatePasswordRequest) {
        val user = userRepository.findByEmail(request.email) ?: throw NotFoundUserEmailException()
        val token = redisRepository
            .getValue(
                "user:${request.email}:${EmailSendType
                    .RESET_PASSWORD
                    .name
                    .lowercase(Locale.getDefault())}_token"
            )
            ?: throw InvalidUserResetPasswordStateException()
        if (token != request.token) throw NotEqualTokenException()

        user.updatePassword(request.password)
        userRepository.save(user)

        redisRepository.delValue(
            "user:${request.email}:${EmailSendType
                .RESET_PASSWORD
                .name
                .lowercase(Locale.getDefault())}_token"
        )
    }

    @Transactional
    fun updateUser(request: UpdateUserRequest, email: String): UpdateUserResponse {
        val user = userRepository.findByEmail(email)
            ?: throw NotFoundUserEmailException()
        user.updateUser(request)
        userRepository.save(user)

        return UpdateUserResponse(user)
    }
}

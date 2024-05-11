package com.restaurant.be.user.domain.service

import com.restaurant.be.common.exception.NotFoundUserException
import com.restaurant.be.user.presentation.dto.GetUserResponse
import com.restaurant.be.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class GetUserService(
    private val userRepository: UserRepository
) {

    fun getUser(userId: Long): GetUserResponse {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundUserException()
        }

        return GetUserResponse(user = user)
    }
}

package com.restaurant.be.user.domain.service

import com.restaurant.be.common.exception.DuplicateUserNicknameException
import com.restaurant.be.user.presentation.dto.CheckNicknameResponse
import com.restaurant.be.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class CheckNicknameService(
    private val userRepository: UserRepository
) {
    fun checkNickname(nickname: String): CheckNicknameResponse {
        userRepository.findByNickname(nickname)?.let {
            throw DuplicateUserNicknameException()
        }
        return CheckNicknameResponse(false)
    }
}

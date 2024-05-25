package com.restaurant.be.user.domain.service

import com.restaurant.be.common.exception.NotFoundUserException
import com.restaurant.be.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeleteUserService(
    private val userRepository: UserRepository
) {
    @Transactional
    fun deleteUser(email: String) {
        val user = userRepository.findByEmail(email) ?: throw NotFoundUserException()

        user.delete()
        userRepository.save(user)
    }
}

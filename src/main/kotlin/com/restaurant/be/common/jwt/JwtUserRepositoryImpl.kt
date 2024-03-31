package com.restaurant.be.common.jwt

import com.restaurant.be.common.exception.NotFoundUserEmailException
import com.restaurant.be.common.exception.WithdrawalUserException
import com.restaurant.be.user.repository.UserRepository
import org.springframework.stereotype.Repository

@Repository
class JwtUserRepositoryImpl(
    private val userRepository: UserRepository
) : JwtUserRepository {

    override fun validTokenByEmail(email: String): Boolean {
        val user = userRepository.findByEmail(email) ?: return false
        return !user.withdrawal
    }

    override fun userRolesByEmail(email: String): List<String> {
        val user = userRepository.findByEmail(email) ?: throw NotFoundUserEmailException()
        if (user.withdrawal) {
            throw WithdrawalUserException()
        }

        return user.roles
    }
}

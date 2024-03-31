package com.restaurant.be.user.repository

import com.restaurant.be.user.domain.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {

    fun findByIdAndWithdrawalFalse(userId: Long): User?
    fun findByNicknameOrEmail(nickname: String, email: String): User?

    fun findByEmail(email: String): User?
}

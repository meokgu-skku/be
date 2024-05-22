package com.restaurant.be.common.util

import com.restaurant.be.user.domain.entity.User
import com.restaurant.be.user.repository.UserRepository

fun setUpUser(email: String, userRepository: UserRepository) {
    val user = User(email = email, profileImageUrl = "")
    userRepository.save(user)
}
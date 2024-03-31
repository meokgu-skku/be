package com.restaurant.be.common.jwt

interface JwtUserRepository {

    fun validTokenByEmail(email: String): Boolean
    fun userRolesByEmail(email: String): List<String>
}

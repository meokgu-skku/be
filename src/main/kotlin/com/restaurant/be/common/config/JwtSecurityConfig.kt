package com.restaurant.be.common.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.restaurant.be.common.jwt.JwtFilter
import com.restaurant.be.common.jwt.JwtUserRepository
import com.restaurant.be.common.jwt.TokenProvider
import com.restaurant.be.common.redis.RedisRepository
import org.springframework.security.config.annotation.SecurityConfigurerAdapter
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

class JwtSecurityConfig(
    private val tokenProvider: TokenProvider,
    private val jwtUserRepository: JwtUserRepository,
    private val redisRepository: RedisRepository,
    private val objectMapper: ObjectMapper
) : SecurityConfigurerAdapter<DefaultSecurityFilterChain?, HttpSecurity>() {

    override fun configure(http: HttpSecurity) {
        val customFilter =
            JwtFilter(tokenProvider, jwtUserRepository, redisRepository, objectMapper)
        http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter::class.java)
    }
}

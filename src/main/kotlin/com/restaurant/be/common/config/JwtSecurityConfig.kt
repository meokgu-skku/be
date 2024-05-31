package com.restaurant.be.common.config

import com.restaurant.be.common.jwt.JwtFilter
import com.restaurant.be.common.jwt.JwtUserRepository
import com.restaurant.be.common.jwt.TokenProvider
import org.springframework.security.config.annotation.SecurityConfigurerAdapter
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

class JwtSecurityConfig(
    private val tokenProvider: TokenProvider,
    private val jwtUserRepository: JwtUserRepository
) : SecurityConfigurerAdapter<DefaultSecurityFilterChain?, HttpSecurity>() {

    override fun configure(http: HttpSecurity) {
        val customFilter =
            JwtFilter(tokenProvider, jwtUserRepository)
        http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter::class.java)
    }
}

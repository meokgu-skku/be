package com.restaurant.be.common.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.restaurant.be.common.jwt.JwtAuthenticationEntryPoint
import com.restaurant.be.common.jwt.JwtUserRepository
import com.restaurant.be.common.jwt.TokenProvider
import com.restaurant.be.common.redis.RedisRepository
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    val tokenProvider: TokenProvider,
    val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
    val jwtUserRepository: JwtUserRepository,
    val redisRepository: RedisRepository,
    val objectMapper: ObjectMapper
) : WebSecurityConfigurerAdapter() {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    override fun configure(http: HttpSecurity) {
        http
            .csrf().disable()
            .formLogin().disable().exceptionHandling().and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .authorizeRequests()
            .antMatchers(
                "/v1/users/email/sign-up",
                "/v1/users/email/sign-in",
                "/v1/users/email/send",
                "/v1/users/email/validate",
                "/v1/users/password",

                "/hello",
                "/profile",

                "/swagger-ui/**",
                "/swagger-resources/**",
                "/v2/api-docs",
                "/webjars/**"
            ).permitAll().anyRequest().authenticated().and()
            .exceptionHandling { it.authenticationEntryPoint(jwtAuthenticationEntryPoint) }
            .apply(
                JwtSecurityConfig(
                    tokenProvider,
                    jwtUserRepository
                )
            )
    }
}

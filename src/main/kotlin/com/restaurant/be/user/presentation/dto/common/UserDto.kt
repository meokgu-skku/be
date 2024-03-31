package com.restaurant.be.user.presentation.dto.common

import io.swagger.annotations.ApiModelProperty

data class UserDto(
    @ApiModelProperty(value = "이메일 아이디", example = "test@gmail.com", required = true)
    val email: String = "",

    @ApiModelProperty(value = "비밀번호", example = "test12!@", required = true)
    val password: String = "",

    @ApiModelProperty(value = "닉네임", example = "닉네임", required = true)
    val nickname: String = ""
)

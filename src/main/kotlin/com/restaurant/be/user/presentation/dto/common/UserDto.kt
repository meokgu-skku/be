package com.restaurant.be.user.presentation.dto.common

import io.swagger.annotations.ApiModelProperty

data class UserDto(
    @ApiModelProperty(value = "유저 아이디", example = "1", required = true)
    val id: Long = 0,

    @ApiModelProperty(value = "이메일 아이디", example = "test@gmail.com", required = true)
    val email: String = "",

    @ApiModelProperty(value = "비밀번호", example = "test12!@", required = true)
    val password: String = "",

    @ApiModelProperty(value = "닉네임", example = "닉네임", required = true)
    val nickname: String = "",

    @ApiModelProperty(
        value = "프로필 이미지 URL",
        example = "https://test.com/test.jpg",
        required = true
    )
    val profileImageUrl: String = ""
)

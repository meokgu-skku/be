package com.restaurant.be.user.presentation.dto

import com.restaurant.be.common.response.Token
import com.restaurant.be.user.domain.entity.User
import com.restaurant.be.user.presentation.dto.common.UserDto
import io.swagger.annotations.ApiModelProperty
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class SignInUserRequest(
    @Email(message = "이메일 형식이 아닙니다.")
    @NotBlank(message = "아이디를 입력해 주세요.")
    @ApiModelProperty(value = "이메일 아이디", example = "test@gmail.com", required = true)
    val email: String,

    @Size(min = 8, max = 20, message = "8~20자 이내로 입력해 주세요.")
    @NotBlank(message = "비밀번호를 입력해 주세요.")
    @ApiModelProperty(value = "비밀번호", example = "test12!@", required = true)
    val password: String
)

data class SignInUserResponse(
    @Schema(description = "유저 정보")
    val userDto: UserDto,
    @Schema(description = "토큰 정보")
    val token: Token
) {
    constructor(user: User, token: Token) : this(
        userDto = UserDto(
            id = user.id ?: 0,
            email = user.email,
            nickname = user.nickname,
            profileImageUrl = user.profileImageUrl
        ),
        token = token
    )
}

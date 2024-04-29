package com.restaurant.be.user.presentation.dto

import com.restaurant.be.common.jwt.Role
import com.restaurant.be.common.password.PasswordService
import com.restaurant.be.common.response.Token
import com.restaurant.be.user.domain.entity.User
import com.restaurant.be.user.presentation.dto.common.UserDto
import io.swagger.annotations.ApiModelProperty
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class SignUpUserRequest(
    @field:Email(message = "이메일 형식이 아닙니다.")
    @field:NotBlank(message = "아이디를 입력해 주세요.")
    @ApiModelProperty(value = "이메일 아이디", example = "test@gmail.com", required = true)
    val email: String = "",

    @field:Size(min = 8, max = 20, message = "8~20자 이내로 입력해 주세요.")
    @field:NotBlank(message = "비밀번호를 입력해 주세요.")
    @ApiModelProperty(value = "비밀번호", example = "test12!@", required = true)
    val password: String = "",

    @field:NotBlank(message = "닉네임을 입력해 주세요.")
    @ApiModelProperty(value = "닉네임", example = "닉네임", required = true)
    val nickname: String = "",

    @ApiModelProperty(
        value = "프로필 이미지 URL",
        example = "https://test.com/test.jpg",
        required = true
    )
    val profileImageUrl: String = ""
) {

    fun toEntity() = User(
        email = email,
        password = password.run(PasswordService::hashPassword),
        nickname = nickname,
        roles = listOf(Role.ROLE_USER.toString())
    )
}

data class SignUpUserResponse(
    @Schema(description = "유저 정보")
    val userDto: UserDto,
    @Schema(description = "토큰 정보")
    val token: Token
) {
    constructor(user: User, token: Token) : this(
        userDto = UserDto(
            email = user.email,
            nickname = user.nickname
        ),
        token = token
    )
}

package com.restaurant.be.user.presentation.dto

import com.restaurant.be.user.domain.entity.User
import com.restaurant.be.user.presentation.dto.common.UserDto
import io.swagger.annotations.ApiModelProperty
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotBlank

data class UpdateUserRequest(
    @field:NotBlank(message = "닉네임을 입력해 주세요.")
    @ApiModelProperty(value = "닉네임", example = "닉네임", required = true)
    val nickname: String = "",

    @ApiModelProperty(
        value = "프로필 이미지 URL",
        example = "https://test.com/test.jpg",
        required = true
    )
    val profileImageUrl: String = ""
)

data class UpdateUserResponse(
    @Schema(description = "유저 정보")
    val userDto: UserDto
) {
    constructor(user: User) : this(
        userDto = UserDto(
            id = user.id ?: 0,
            email = user.email,
            nickname = user.nickname,
            profileImageUrl = user.profileImageUrl
        )
    )
}

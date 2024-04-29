@file:Suppress("ktlint", "MatchingDeclarationName")

package com.restaurant.be.user.presentation.dto

import com.restaurant.be.user.presentation.dto.common.EmailSendType
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty

data class SendEmailRequest(
    @field:NotEmpty(message = "이메일은 필수 값 입니다.")
    @field:Email(message = "유효하지 않는 이메일 입니다.")
    @ApiModelProperty(value = "이메일", example = "test@test.com", required = true)
    val email: String,

    @ApiModelProperty(
        value = "이메일 전송 타입",
        example = "EMAIL_VALIDATION",
        required = true,
        allowableValues = "EMAIL_VALIDATION, UPDATE_PASSWORD"
    )
    val sendType: EmailSendType
)

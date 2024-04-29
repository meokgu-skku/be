package com.restaurant.be.user.presentation.controller

import com.restaurant.be.common.response.CommonResponse
import com.restaurant.be.user.presentation.dto.SendEmailRequest
import com.restaurant.be.user.presentation.dto.ValidateEmailRequest
import com.restaurant.be.user.presentation.dto.ValidateEmailResponse
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@Api(tags = ["01. User Info"], description = "유저 서비스")
@RestController
@RequestMapping("/v1/users/email")
class ValidateEmailController {
    @PostMapping("/send")
    @ApiOperation(value = "이메일 전송 API")
    @ApiResponse(
        responseCode = "200",
        description = "성공"
    )
    fun sendEmail(
        @Valid @RequestBody
        request: SendEmailRequest
    ): CommonResponse<Unit> {
        return CommonResponse.success()
    }

    @PostMapping("/validate")
    @ApiOperation(value = "이메일 인증 API")
    @ApiResponse(
        responseCode = "200",
        description = "성공",
        content = [Content(schema = Schema(implementation = ValidateEmailResponse::class))]
    )
    fun validateEmail(
        @Valid @RequestBody
        request: ValidateEmailRequest
    ): CommonResponse<ValidateEmailResponse> {
        return CommonResponse.success(ValidateEmailResponse("test"))
    }
}

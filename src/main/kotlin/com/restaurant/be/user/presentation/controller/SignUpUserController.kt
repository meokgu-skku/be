package com.restaurant.be.user.presentation.controller

import com.restaurant.be.common.response.CommonResponse
import com.restaurant.be.user.domain.service.SignUpUserService
import com.restaurant.be.user.presentation.dto.SignUpUserRequest
import com.restaurant.be.user.presentation.dto.SignUpUserResponse
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
class SignUpUserController(
    private val signUpUserService: SignUpUserService
) {

    @PostMapping("/sign-up")
    @ApiOperation(value = "유저 이메일 회원가입 API")
    @ApiResponse(
        responseCode = "200",
        description = "성공",
        content = [Content(schema = Schema(implementation = SignUpUserResponse::class))]
    )
    fun signUpUser(
        @Valid @RequestBody
        request: SignUpUserRequest
    ): CommonResponse<SignUpUserResponse> {
        val response = signUpUserService.signUpUser(request)
        return CommonResponse.success(response)
    }
}

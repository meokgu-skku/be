package com.restaurant.be.user.presentation.controller

import com.restaurant.be.common.response.CommonResponse
import com.restaurant.be.user.domain.service.SignInUserService
import com.restaurant.be.user.presentation.dto.SignInUserRequest
import com.restaurant.be.user.presentation.dto.SignInUserResponse
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
class SignInUserController(
    private val signInUserService: SignInUserService
) {

    @PostMapping("/sign-in")
    @ApiOperation(value = "유저 이메일 로그인 API")
    @ApiResponse(
        responseCode = "200",
        description = "성공",
        content = [Content(schema = Schema(implementation = SignInUserResponse::class))]
    )
    fun signInUser(
        @Valid @RequestBody
        request: SignInUserRequest
    ): CommonResponse<SignInUserResponse> {
        val response = signInUserService.signInUser(request)
        return CommonResponse.success(response)
    }
}

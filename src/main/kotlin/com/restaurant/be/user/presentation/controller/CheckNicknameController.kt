package com.restaurant.be.user.presentation.controller

import com.restaurant.be.common.response.CommonResponse
import com.restaurant.be.user.domain.service.CheckNicknameService
import com.restaurant.be.user.presentation.dto.CheckNicknameResponse
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Api(tags = ["01. User Info"], description = "유저 서비스")
@RestController
@RequestMapping("/v1/users")
class CheckNicknameController(
    private val checkNicknameService: CheckNicknameService
) {

    @GetMapping("/check-nickname")
    @ApiOperation(value = "유저 정보 조회 API")
    @ApiResponse(
        responseCode = "200",
        description = "성공",
        content = [Content(schema = Schema(implementation = CheckNicknameResponse::class))]
    )
    fun checkNickname(@RequestParam nickname: String): CommonResponse<CheckNicknameResponse> {
        val response = checkNicknameService.checkNickname(nickname)
        return CommonResponse.success(response)
    }
}

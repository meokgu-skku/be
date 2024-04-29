package com.restaurant.be.user.presentation.controller

import com.restaurant.be.common.response.CommonResponse
import com.restaurant.be.user.presentation.dto.UpdateUserRequest
import com.restaurant.be.user.presentation.dto.UpdateUserResponse
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import javax.validation.Valid

@Api(tags = ["01. User Info"], description = "유저 서비스")
@RestController
@RequestMapping("/api/v1/users")
class UpdateUserController {

    @PatchMapping
    @PreAuthorize("hasRole('USER')")
    @ApiOperation(value = "유저 수정 API")
    @ApiResponse(
        responseCode = "200",
        description = "성공",
        content = [Content(schema = Schema(implementation = UpdateUserResponse::class))]
    )
    fun updateUser(
        principal: Principal,
        @Valid @RequestBody
        request: UpdateUserRequest
    ): CommonResponse<UpdateUserResponse> {
        return CommonResponse.success()
    }
}

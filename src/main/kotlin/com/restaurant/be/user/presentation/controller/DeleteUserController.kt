package com.restaurant.be.user.presentation.controller

import com.restaurant.be.common.response.CommonResponse
import com.restaurant.be.user.domain.service.DeleteUserService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@Api(tags = ["01. User Info"], description = "유저 서비스")
@RestController
@RequestMapping("/v1/users")
class DeleteUserController(
    private val deleteUserService: DeleteUserService
) {

    @DeleteMapping
    @PreAuthorize("hasRole('USER')")
    @ApiOperation(value = "유저 삭제 API")
    @ApiResponse(
        responseCode = "200",
        description = "성공"
    )
    fun deleteUser(principal: Principal): CommonResponse<Void> {
        deleteUserService.deleteUser(principal.name)
        return CommonResponse.success()
    }
}

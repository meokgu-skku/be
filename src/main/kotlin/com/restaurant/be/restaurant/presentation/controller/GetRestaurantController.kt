package com.restaurant.be.restaurant.presentation.controller

import com.restaurant.be.common.response.CommonResponse
import com.restaurant.be.restaurant.presentation.domain.service.GetRestaurantByIdService
import com.restaurant.be.restaurant.presentation.dto.GetRestaurantRequest
import com.restaurant.be.restaurant.presentation.dto.GetRestaurantResponse
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@Api(tags = ["02. Restaurant Info"], description = "음식점 서비스")
@RestController
@RequestMapping("/api/v1/restaurants")
class GetRestaurantController(
    private val getRestaurantByIdService: GetRestaurantByIdService
) {

    @PostMapping("/{restaurantId}")
    @PreAuthorize("hasRole('USER')")
    @ApiOperation(value = "음식점 상세 조회 API")
    @ApiResponse(
        responseCode = "200",
        description = "성공",
        content = [Content(schema = Schema(implementation = GetRestaurantResponse::class))]
    )
    fun getRestaurant(
        @PathVariable
        restaurantId: Long,
        @Valid @RequestBody
        request: GetRestaurantRequest
    ): CommonResponse<GetRestaurantResponse> {
        println("GetRestaurantController")
        val response = getRestaurantByIdService.getRestaurantById(restaurantId, request.userId)
        return CommonResponse.success(response)
    }
}

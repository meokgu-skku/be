package com.restaurant.be.restaurant.presentation.controller

import com.restaurant.be.common.response.CommonResponse
import com.restaurant.be.restaurant.presentation.dto.RecommendRestaurantResponse
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@Api(tags = ["02. Restaurant Info"], description = "음식점 서비스")
@RestController
@RequestMapping("/api/v1/restaurants")
class RecommendRestaurantController {

    @GetMapping("recommend")
    @PreAuthorize("hasRole('USER')")
    @ApiOperation(value = "gpt 기반 추천 음식점 리스트 조회 API")
    @ApiResponse(
        responseCode = "200",
        description = "성공",
        content = [Content(schema = Schema(implementation = RecommendRestaurantResponse::class))]
    )
    fun getRecommendRestaurants(
        principal: Principal
    ): CommonResponse<RecommendRestaurantResponse> {
        return CommonResponse.success()
    }
}

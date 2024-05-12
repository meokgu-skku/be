package com.restaurant.be.restaurant.presentation.controller

import com.restaurant.be.common.response.CommonResponse
import com.restaurant.be.restaurant.presentation.domain.service.GetCategoryService
import com.restaurant.be.restaurant.presentation.dto.GetCategoryRequest
import com.restaurant.be.restaurant.presentation.dto.GetCategoryResponse
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@Api(tags = ["02. Restaurant Info"], description = "음식점 서비스")
@RestController
@RequestMapping("/api/v1/restaurants/category")
class GetCategoryController(
    private val getCategoryService: GetCategoryService
) {

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @ApiOperation(value = "해당 카테고리 전체 조회 API")
    @ApiResponse(
        responseCode = "200",
        description = "성공",
        content = [Content(schema = Schema(implementation = GetCategoryResponse::class))]
    )
    fun getCategory(
        @Valid @RequestBody
        request: GetCategoryRequest
    ): CommonResponse<GetCategoryResponse> {
        val response = getCategoryService.getCategory(request)
        return CommonResponse.success(response)
    }
}

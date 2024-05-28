package com.restaurant.be.category.presentation.controller

import com.restaurant.be.category.domain.service.GetCategoryService
import com.restaurant.be.category.presentation.controller.dto.GetCategoriesResponse
import com.restaurant.be.common.response.CommonResponse
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Api(tags = ["02. Restaurant Info"], description = "음식점 서비스")
@RestController
@RequestMapping("/v1/restaurants/category")
class GetCategoryController(
    private val getCategoryService: GetCategoryService
) {

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @ApiOperation(value = "카테고리 전체 조회 API")
    @ApiResponse(
        responseCode = "200",
        description = "성공",
        content = [Content(schema = Schema(implementation = GetCategoriesResponse::class))]
    )
    fun getCategories(): CommonResponse<GetCategoriesResponse> {
        val response = getCategoryService.getCategories()
        return CommonResponse.success(response)
    }
}

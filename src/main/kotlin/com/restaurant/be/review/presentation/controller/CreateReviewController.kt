package com.restaurant.be.review.presentation.controller

import com.restaurant.be.common.response.CommonResponse
import com.restaurant.be.review.domain.service.CreateReviewService
import com.restaurant.be.review.presentation.dto.CreateReviewResponse
import com.restaurant.be.review.presentation.dto.common.ReviewRequestDto
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
import java.security.Principal
import javax.validation.Valid

@Api(tags = ["03. Review Info"], description = "리뷰 서비스")
@RestController
@RequestMapping("/api/v1/restaurants")
class CreateReviewController(
    private val createReviewService: CreateReviewService
) {

    @PostMapping("/{restaurantId}/reviews")
    @PreAuthorize("hasRole('USER')")
    @ApiOperation(value = "리뷰 작성 API")
    @ApiResponse(
        responseCode = "200",
        description = "성공",
        content = [Content(schema = Schema(implementation = CreateReviewResponse::class))]
    )
    fun createReview(
        principal: Principal,
        @PathVariable restaurantId: Long,
        @Valid @RequestBody
        createReview: ReviewRequestDto
    ): CommonResponse<CreateReviewResponse> {
        val response = createReviewService.createReviewOf(restaurantId, createReview, principal.name)
        return CommonResponse.success(response)
    }
}

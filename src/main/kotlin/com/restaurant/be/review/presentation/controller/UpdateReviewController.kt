package com.restaurant.be.review.presentation.controller

import com.restaurant.be.common.response.CommonResponse
import com.restaurant.be.review.domain.service.UpdateReviewService
import com.restaurant.be.review.presentation.dto.UpdateReviewResponse
import com.restaurant.be.review.presentation.dto.common.ReviewRequestDto
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import javax.validation.Valid

@Api(tags = ["03. Review Info"], description = "리뷰 서비스")
@RestController
@RequestMapping("/v1/restaurants/reviews")
class UpdateReviewController (
    val updateReviewService: UpdateReviewService
){

    @PatchMapping("/{restaurantId}/reviews/{reviewId}")
    @PreAuthorize("hasRole('USER')")
    @ApiOperation(value = "리뷰 수정 API")
    @ApiResponse(
        responseCode = "200",
        description = "성공",
        content = [Content(schema = Schema(implementation = UpdateReviewResponse::class))]
    )
    fun updateReview(
        principal: Principal,
        @PathVariable restaurantId: Long,
        @PathVariable reviewId: Long,
        @RequestBody @Valid
        request: ReviewRequestDto
    ): CommonResponse<UpdateReviewResponse> {
        val response = updateReviewService.updateReview(restaurantId, reviewId, request, principal.name)
        return CommonResponse.success(response)
    }
}

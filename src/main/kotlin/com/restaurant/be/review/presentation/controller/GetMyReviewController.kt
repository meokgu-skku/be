package com.restaurant.be.review.presentation.controller

import com.restaurant.be.common.response.CommonResponse
import com.restaurant.be.review.domain.service.GetReviewService
import com.restaurant.be.review.presentation.dto.GetMyReviewResponse
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@Api(tags = ["03. Review Info"], description = "리뷰 서비스")
@RestController
@RequestMapping("/api/v1/restaurants/my-reviews")
class GetMyReviewController(
    val getReviewService: GetReviewService
) {

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @ApiOperation(value = "내가 작성한 리뷰 리스트 조회 API")
    @ApiResponse(
        responseCode = "200",
        description = "성공",
        content = [Content(schema = Schema(implementation = GetMyReviewResponse::class))]
    )
    fun getMyReview(
        principal: Principal,
        pageable: Pageable
    ): CommonResponse<GetMyReviewResponse> {
        val response = getReviewService.getMyReviewList(pageable, principal.name)
        return CommonResponse.success(response)
    }
}

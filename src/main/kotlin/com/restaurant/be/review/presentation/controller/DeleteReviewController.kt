package com.restaurant.be.review.presentation.controller

import com.restaurant.be.common.response.CommonResponse
import com.restaurant.be.review.domain.service.DeleteReviewService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@Api(tags = ["03. Review Info"], description = "리뷰 서비스")
@RestController
@RequestMapping("/v1/restaurants/")
class DeleteReviewController(
    val deleteReviewService: DeleteReviewService
) {

    @DeleteMapping("/reviews/{reviewId}")
    @PreAuthorize("hasRole('USER')")
    @ApiOperation(value = "리뷰 삭제 API")
    @ApiResponse(
        responseCode = "200",
        description = "성공"
    )
    fun deleteReview(
        principal: Principal,
        @PathVariable reviewId: Long
    ): CommonResponse<Void> {
        deleteReviewService.deleteReview(reviewId, principal.name)
        return CommonResponse.success()
    }
}

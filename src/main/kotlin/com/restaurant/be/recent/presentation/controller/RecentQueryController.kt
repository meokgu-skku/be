package com.restaurant.be.recent.presentation.controller

import com.restaurant.be.common.response.CommonResponse
import com.restaurant.be.recent.presentation.dto.DeleteRecentQueriesRequest
import com.restaurant.be.recent.presentation.dto.RecentQueriesResponse
import com.restaurant.be.recent.service.RecentQueryService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import javax.validation.Valid

@Api(tags = ["04. Recent Info"], description = "최근검색어 서비스")
@RestController
@RequestMapping("/v1/recents")
class RecentQueryController(
    private val recentQueryService: RecentQueryService
) {

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @ApiOperation(value = "최근검색어 조회 API")
    @ApiResponse(
        responseCode = "200",
        description = "성공",
        content = [Content(schema = Schema(implementation = RecentQueriesResponse::class))]
    )
    fun getRecentQueries(principal: Principal): CommonResponse<RecentQueriesResponse> {
        val response = recentQueryService.getRecentQueries(principal.name)
        return CommonResponse.success(response)
    }

    @DeleteMapping
    @PreAuthorize("hasRole('USER')")
    @ApiOperation(value = "최근검색어 삭제 API")
    @ApiResponse(
        responseCode = "200",
        description = "성공",
        content = [Content(schema = Schema(implementation = RecentQueriesResponse::class))]
    )
    fun deleteRecentQueries(
        principal: Principal,
        @Valid @RequestBody
        request: DeleteRecentQueriesRequest
    ): CommonResponse<RecentQueriesResponse> {
        val response = recentQueryService.deleteRecentQueries(principal.name, request)
        return CommonResponse.success(response)
    }
}

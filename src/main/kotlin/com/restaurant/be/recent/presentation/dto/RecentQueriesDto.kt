package com.restaurant.be.recent.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

data class DeleteRecentQueriesRequest(
    @Schema(description = "삭제할 검색어 (query 없이 요청하면 전체삭제)")
    val query: String?
)

data class RecentQueriesResponse(
    val recentQueries: List<RecentQueriesDto>
)

data class RecentQueriesDto(
    @Schema(description = "최근 검색어")
    val query: String
)

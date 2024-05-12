package com.restaurant.be.review.presentation.dto.common

import com.restaurant.be.review.domain.entity.Review
import com.restaurant.be.user.domain.entity.User
import io.swagger.annotations.ApiModelProperty
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class ReviewRequestDto(
    @Schema(description = "평가 점수")
    @field:NotNull(message = "평가 점수를 입력해주세요")
    @ApiModelProperty(value = "별점", example = "5", required = true)
    val rating: Int,

    @Schema(description = "리뷰 내용")
    @field:NotBlank(message = "리뷰 내용을 작성해주세요")
    @ApiModelProperty(value = "리뷰 내용", example = "사장님이 친절해요", required = true)
    val comment: String,

    @Schema(description = "이미지 url 리스트")
    val imageUrls: List<String>,

    @Schema(description = "좋아요 여부")
    @field:NotNull(message = "식당 추천 여부를 선택해주세요")
    @ApiModelProperty(value = "리뷰 내용", example = "true", required = true)
    val isLike: Boolean
)

data class ReviewResponseDto(
    @Schema(description = "유저 id")
    val userId: Long,
    @Schema(description = "식당 id")
    val restaurantId: Long,
    @Schema(description = "평가 점수")
    val rating: Int,
    @Schema(description = "리뷰 내용")
    val comment: String,
    @Schema(description = "이미지 url 리스트")
    val imageUrls: List<String>,
    @Schema(description = "좋아요 여부")
    val isLike: Boolean
)

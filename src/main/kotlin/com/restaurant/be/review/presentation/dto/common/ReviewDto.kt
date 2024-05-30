package com.restaurant.be.review.presentation.dto.common

import com.restaurant.be.review.domain.entity.Review
import com.restaurant.be.user.domain.entity.User
import io.swagger.annotations.ApiModelProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class ReviewRequestDto(
    @Schema(description = "평가 점수")
    @field:NotNull(message = "평가 점수를 입력해주세요")
    @ApiModelProperty(value = "별점", example = "5", required = true)
    val rating: Double,

    @Schema(description = "리뷰 내용")
    @field:NotBlank(message = "리뷰 내용을 작성해주세요")
    @ApiModelProperty(value = "리뷰 내용", example = "사장님이 친절해요", required = true)
    val content: String,

    @Schema(description = "이미지 url 리스트")
    val imageUrls: List<String>
) {
    fun toEntity(user: User, restaurantId: Long) = Review(
        user = user,
        rating = rating,
        content = content,
        restaurantId = restaurantId
    )
}

data class ReviewResponseDto(
    @Schema(description = "리뷰 id")
    val id: Long,
    @Schema(description = "유저 id")
    val userId: Long,
    @Schema(description = "유저 닉네임")
    val username: String,
    @Schema(description = "유저 프로필 이미지")
    val profileImageUrl: String,
    @Schema(description = "식당 id")
    val restaurantId: Long,
    @Schema(description = "평가 점수")
    val rating: Double,
    @Schema(description = "리뷰 내용")
    val content: String,
    @Schema(description = "이미지 url 리스트")
    val imageUrls: List<String>,
    @Schema(description = "좋아요 여부")
    val isLike: Boolean,
    @Schema(description = "좋아요 받은 횟수")
    val likeCount: Long,
    @Schema(description = "리뷰 조회 수")
    val viewCount: Long,
    @Schema(description = "리뷰 생성 시간")
    val createdAt: LocalDateTime,
    @Schema(description = "리뷰 수정 시간")
    val modifiedAt: LocalDateTime
) {
    companion object {
        fun toDto(review: Review, isLikedByUser: Boolean): ReviewResponseDto {
            return ReviewResponseDto(
                id = review.id ?: 0,
                userId = review.user.id ?: 0,
                username = review.user.nickname,
                profileImageUrl = review.user.profileImageUrl,
                restaurantId = review.restaurantId,
                rating = review.rating,
                content = review.content,
                imageUrls = review.images.map { it.imageUrl },
                isLike = isLikedByUser,
                likeCount = review.likeCount,
                viewCount = review.viewCount,
                createdAt = review.createdAt,
                modifiedAt = review.modifiedAt
            )
        }
    }
}

package com.restaurant.be.review

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.restaurant.be.common.CustomDescribeSpec
import com.restaurant.be.common.IntegrationTest
import com.restaurant.be.common.response.CommonResponse
import com.restaurant.be.review.presentation.dto.CreateReviewResponse
import com.restaurant.be.review.presentation.dto.common.ReviewRequestDto
import com.restaurant.be.user.domain.service.SignUpUserService
import com.restaurant.be.user.presentation.dto.SignUpUserRequest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.testcontainers.shaded.org.bouncycastle.cms.RecipientId.password
import java.nio.charset.StandardCharsets

@IntegrationTest
class ReviewIntegrationTest(
    @Autowired
    private val mockMvc: MockMvc,
    @Autowired
    private val objectMapper: ObjectMapper,
    @Autowired
    private val signUpUserService: SignUpUserService
) : CustomDescribeSpec() {
    private val mockRestaurantID = "1"
    private val resource = "reviews"

    @WithMockUser(username = "test@gmail.com", roles = ["USER"], password = "a12345678")
    @Test
    fun `사진 없는 리뷰 작성(RequestDto에서 Image List만 비어있을 경우)시 성공한다`() {
        signUpUserService.signUpUser(
            SignUpUserRequest(
                email = "test@gmail.com",
                password = "a12345678",
                nickname = "testname"
            )
        )
        val reviewRequest = ReviewRequestDto(
            rating = 3,
            comment = "맛있어요",
            imageUrls = listOf(),
            isLike = true
        )
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/restaurants/{restaurantID}/$resource", mockRestaurantID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewRequest))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.result").value("SUCCESS"))
            .andReturn()

        val actualResult: CommonResponse<CreateReviewResponse> = objectMapper.readValue(
            result.response.contentAsString.toByteArray(StandardCharsets.ISO_8859_1),
            object : TypeReference<CommonResponse<CreateReviewResponse>>() {}
        )

        actualResult.data!!.review.content shouldBe "맛있어요"
        actualResult.data!!.review.images.size shouldBe 0
    }

    @WithMockUser(username = "test@gmail.com", roles = ["USER"], password = "a12345678")
    @Test
    fun`comment가 없으면 오류 반환`() {
        val reviewRequest = ReviewRequestDto(
            rating = 3,
            comment = "",
            imageUrls = listOf(),
            isLike = true
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/restaurants/{restaurantID}/$resource", mockRestaurantID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewRequest))
        ) // 400 수정 예정
            .andExpect(MockMvcResultMatchers.status().isInternalServerError)
    }
}

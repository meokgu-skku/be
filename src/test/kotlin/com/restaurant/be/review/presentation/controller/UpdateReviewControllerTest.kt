package com.restaurant.be.review.presentation.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.restaurant.be.common.CustomDescribeSpec
import com.restaurant.be.common.IntegrationTest
import com.restaurant.be.common.PageDeserializer
import com.restaurant.be.common.response.CommonResponse
import com.restaurant.be.common.util.RestaurantUtil
import com.restaurant.be.common.util.ReviewUtil
import com.restaurant.be.common.util.setUpUser
import com.restaurant.be.restaurant.repository.RestaurantRepository
import com.restaurant.be.review.presentation.dto.UpdateReviewRequest
import com.restaurant.be.review.presentation.dto.UpdateReviewResponse
import com.restaurant.be.review.presentation.dto.common.ReviewRequestDto
import com.restaurant.be.review.presentation.dto.common.ReviewResponseDto
import com.restaurant.be.review.repository.ReviewRepository
import com.restaurant.be.user.domain.entity.User
import com.restaurant.be.user.repository.UserRepository
import io.kotest.matchers.shouldBe
import org.springframework.data.domain.Page
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.Charset

@IntegrationTest
@Transactional
class UpdateReviewControllerTest(
    private val mockMvc: MockMvc,
    private val userRepository: UserRepository,
    private val restaurantRepository: RestaurantRepository,
    private val reviewRepository: ReviewRepository
) : CustomDescribeSpec() {
    private val baseUrl = "/v1/restaurants"
    private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule()).apply {
        val module = SimpleModule()
        module.addDeserializer(Page::class.java, PageDeserializer(ReviewResponseDto::class.java))
        this.registerModule(module)
        this.registerModule(JavaTimeModule())
    }

    init {
        beforeEach {
            setUpUser("test@gmail.com", userRepository)
        }

        describe("updateReview basic test") {
            it("should return 200") {
                // given
                val restaurant = restaurantRepository.save(
                    RestaurantUtil.generateRestaurantEntity(
                        name = "restaurant"
                    )
                )

                val review = reviewRepository.save(
                    ReviewUtil.generateReviewEntity(
                        restaurantId = restaurant.id,
                        user = userRepository.findByEmail("test@gmail.com")
                            ?: throw Exception()
                    )
                )

                val req = ReviewRequestDto(
                    rating = 5.0,
                    content = "사장님이 친절해요",
                    imageUrls = listOf("https://image.com")
                )

                val request = UpdateReviewRequest(
                    review = req
                )

                // when
                val result = mockMvc.perform(
                    patch("$baseUrl/reviews/${restaurant.id}/reviews/${review.id}")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request))
                ).also {
                    println(it.andReturn().response.contentAsString)
                }
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(
                    Charset.forName(
                        "UTF-8"
                    )
                )
                val responseType =
                    object : TypeReference<CommonResponse<UpdateReviewResponse>>() {}
                val actualResult: CommonResponse<UpdateReviewResponse> =
                    objectMapper.readValue(
                        responseContent,
                        responseType
                    )

                // then
                actualResult.data!!.review.rating shouldBe 5.0
                actualResult.data!!.review.content shouldBe "사장님이 친절해요"
                actualResult.data!!.review.imageUrls shouldBe listOf("https://image.com")
            }

            it("when not existed restaurant's review update should return 404") {
                // given
                val req = ReviewRequestDto(
                    rating = 5.0,
                    content = "사장님이 친절해요",
                    imageUrls = listOf("https://image.com")
                )
                val request = UpdateReviewRequest(
                    review = req
                )

                // when
                val result = mockMvc.perform(
                    patch("$baseUrl/reviews/1/reviews/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request))
                ).also {
                    println(it.andReturn().response.contentAsString)
                }
                    .andExpect(status().isBadRequest)
                    .andExpect(jsonPath("$.result").value("FAIL"))
                    .andReturn()
            }

            it("when another user's review update should return NotFoundReviewException") {
                // given
                val user = userRepository.save(
                    User(email = "test@test.com", profileImageUrl = "")
                )

                val restaurant = restaurantRepository.save(
                    RestaurantUtil.generateRestaurantEntity(
                        name = "restaurant"
                    )
                )

                val review = reviewRepository.save(
                    ReviewUtil.generateReviewEntity(
                        restaurantId = restaurant.id,
                        user = user
                    )
                )

                val req = ReviewRequestDto(
                    rating = 5.0,
                    content = "사장님이 친절해요",
                    imageUrls = listOf("https://image.com")
                )
                val request = UpdateReviewRequest(
                    review = req
                )

                // when
                val result = mockMvc.perform(
                    patch("$baseUrl/reviews/${restaurant.id}/reviews/${review.id}")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request))
                ).also {
                    println(it.andReturn().response.contentAsString)
                }
                    .andExpect(status().isUnauthorized)
                    .andExpect(jsonPath("$.result").value("FAIL"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(
                    Charset.forName(
                        "UTF-8"
                    )
                )
                val responseType =
                    object : TypeReference<CommonResponse<UpdateReviewResponse>>() {}
                val actualResult: CommonResponse<UpdateReviewResponse> =
                    objectMapper.readValue(
                        responseContent,
                        responseType
                    )

                // then
                actualResult.message shouldBe "해당 게시글을 수정할 권한이 없습니다."
            }
        }
    }
}

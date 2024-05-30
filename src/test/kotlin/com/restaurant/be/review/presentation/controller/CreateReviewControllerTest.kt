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
import com.restaurant.be.common.util.setUpUser
import com.restaurant.be.restaurant.presentation.controller.dto.common.RestaurantDto
import com.restaurant.be.restaurant.repository.RestaurantRepository
import com.restaurant.be.review.presentation.dto.CreateReviewResponse
import com.restaurant.be.review.presentation.dto.common.ReviewRequestDto
import com.restaurant.be.user.repository.UserRepository
import io.kotest.matchers.shouldBe
import org.springframework.data.domain.Page
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.Charset

@IntegrationTest
@Transactional
class CreateReviewControllerTest(
    private val mockMvc: MockMvc,
    private val userRepository: UserRepository,
    private val restaurantRepository: RestaurantRepository
) : CustomDescribeSpec() {
    private val baseUrl = "/v1/restaurants"
    private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule()).apply {
        val module = SimpleModule()
        module.addDeserializer(Page::class.java, PageDeserializer(RestaurantDto::class.java))
        this.registerModule(module)
        this.registerModule(JavaTimeModule())
    }

    init {
        beforeEach {
            setUpUser("test@gmail.com", userRepository)
        }

        describe("#createReview basic test") {
            it("when existed restaurant's review create should return success") {
                // given
                val restaurant = restaurantRepository.save(
                    RestaurantUtil.generateRestaurantEntity(
                        name = "restaurant"
                    )
                )
                val request = ReviewRequestDto(
                    rating = 5.0,
                    content = "사장님이 친절해요",
                    imageUrls = listOf("https://image.com")
                )

                // when
                val result = mockMvc.perform(
                    post("$baseUrl/${restaurant.id}/reviews")
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
                    object : TypeReference<CommonResponse<CreateReviewResponse>>() {}
                val actualResult: CommonResponse<CreateReviewResponse> =
                    objectMapper.readValue(
                        responseContent,
                        responseType
                    )

                // then
                actualResult.data!!.review.rating shouldBe 5.0
                actualResult.data!!.review.content shouldBe "사장님이 친절해요"
                actualResult.data!!.review.imageUrls shouldBe listOf("https://image.com")
            }

            it("when not existed restaurant's review create should return 404") {
                // given
                val request = ReviewRequestDto(
                    rating = 5.0,
                    content = "사장님이 친절해요",
                    imageUrls = listOf("https://image.com")
                )

                // when
                val result = mockMvc.perform(
                    post("$baseUrl/12345/reviews")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request))
                ).also {
                    println(it.andReturn().response.contentAsString)
                }
                    .andExpect(status().isNotFound)
                    .andReturn()

                val responseContent = result.response.getContentAsString(
                    Charset.forName(
                        "UTF-8"
                    )
                )
                val responseType =
                    object : TypeReference<CommonResponse<CreateReviewResponse>>() {}
                val actualResult: CommonResponse<CreateReviewResponse> =
                    objectMapper.readValue(
                        responseContent,
                        responseType
                    )

                // then
                actualResult.result shouldBe CommonResponse.Result.FAIL
                actualResult.message shouldBe "해당 식당 정보가 존재하지 않습니다."
            }
        }
    }
}

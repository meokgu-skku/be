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
import com.restaurant.be.restaurant.presentation.controller.dto.common.RestaurantDto
import com.restaurant.be.restaurant.repository.RestaurantRepository
import com.restaurant.be.review.repository.ReviewRepository
import com.restaurant.be.user.repository.UserRepository
import io.kotest.matchers.shouldBe
import org.springframework.data.domain.Page
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.Charset

@IntegrationTest
@Transactional
class DeleteReviewControllerTest(
    private val mockMvc: MockMvc,
    private val userRepository: UserRepository,
    private val restaurantRepository: RestaurantRepository,
    private val reviewRepository: ReviewRepository
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

        describe("#deleteReview basic test") {
            it("when existed review delete should return success") {
                // given
                val restaurant = restaurantRepository.save(
                    RestaurantUtil.generateRestaurantEntity(
                        name = "restaurant"
                    )
                )
                val review = reviewRepository.save(
                    ReviewUtil.generateReviewEntity(
                        restaurantId = restaurant.id,
                        user = userRepository.findByEmail("test@gmail.com") ?: throw Exception()
                    )
                )

                // when
                val result = mockMvc.perform(
                    delete("$baseUrl/reviews/${review.id}")
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
                    object : TypeReference<CommonResponse<Void>>() {}
                val actualResult: CommonResponse<Void> =
                    objectMapper.readValue(
                        responseContent,
                        responseType
                    )

                // then
                actualResult.result shouldBe CommonResponse.Result.SUCCESS
            }

            it("when not existed review delete should return fail") {
                // when
                val result = mockMvc.perform(
                    delete("$baseUrl/reviews/12345")
                ).also {
                    println(it.andReturn().response.contentAsString)
                }
                    .andExpect(status().isBadRequest)
                    .andExpect(jsonPath("$.result").value("FAIL"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(
                    Charset.forName(
                        "UTF-8"
                    )
                )
                val responseType =
                    object : TypeReference<CommonResponse<Void>>() {}
                val actualResult: CommonResponse<Void> =
                    objectMapper.readValue(
                        responseContent,
                        responseType
                    )

                // then
                actualResult.result shouldBe CommonResponse.Result.FAIL
                actualResult.message shouldBe "존재하지 않은 리뷰 입니다."
            }

            it("when not existed restaurant's review delete should return fail") {
                // given
                val review = reviewRepository.save(
                    ReviewUtil.generateReviewEntity(
                        user = userRepository.findByEmail("test@gmail.com") ?: throw Exception(),
                        restaurantId = 12345
                    )
                )

                // when
                val result = mockMvc.perform(
                    delete("$baseUrl/reviews/${review.id}")
                ).also {
                    println(it.andReturn().response.contentAsString)
                }
                    .andExpect(status().isNotFound)
                    .andExpect(jsonPath("$.result").value("FAIL"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(
                    Charset.forName(
                        "UTF-8"
                    )
                )
                val responseType =
                    object : TypeReference<CommonResponse<Void>>() {}
                val actualResult: CommonResponse<Void> =
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

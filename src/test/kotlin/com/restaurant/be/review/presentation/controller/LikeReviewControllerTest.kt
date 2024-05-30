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
import com.restaurant.be.review.domain.entity.ReviewLike
import com.restaurant.be.review.presentation.dto.LikeReviewResponse
import com.restaurant.be.review.presentation.dto.common.ReviewResponseDto
import com.restaurant.be.review.repository.ReviewLikeRepository
import com.restaurant.be.review.repository.ReviewRepository
import com.restaurant.be.user.repository.UserRepository
import io.kotest.matchers.shouldBe
import org.springframework.data.domain.Page
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.Charset

@IntegrationTest
@Transactional
class LikeReviewControllerTest(
    private val mockMvc: MockMvc,
    private val userRepository: UserRepository,
    private val restaurantRepository: RestaurantRepository,
    private val reviewRepository: ReviewRepository,
    private val reviewLikeRepository: ReviewLikeRepository
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

        describe("#likeReview basic test") {
            it("when existed review and user should like review") {
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

                // when
                val result = mockMvc.perform(
                    post("$baseUrl/reviews/${review.id}/like")
                        .contentType("application/json")
                        .content(
                            objectMapper.writeValueAsString(
                                mapOf(
                                    "isLike" to true
                                )
                            )
                        )
                ).also {
                    println(it.andReturn().response.contentAsString)
                }
                    .andExpect(MockMvcResultMatchers.status().isOk)
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(
                    Charset.forName(
                        "UTF-8"
                    )
                )
                val responseType =
                    object : TypeReference<CommonResponse<LikeReviewResponse>>() {}
                val actualResult: CommonResponse<LikeReviewResponse> =
                    objectMapper.readValue(
                        responseContent,
                        responseType
                    )

                // then
                actualResult.data!!.review.isLike shouldBe true
            }

            it("when not existed review should throw NotFoundReviewException") {
                // when
                val result = mockMvc.perform(
                    post("$baseUrl/reviews/1/like")
                        .contentType("application/json")
                        .content(
                            objectMapper.writeValueAsString(
                                mapOf(
                                    "isLike" to true
                                )
                            )
                        )
                ).also {
                    println(it.andReturn().response.contentAsString)
                }
                    .andExpect(MockMvcResultMatchers.status().isBadRequest)
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result").value("FAIL"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("존재하지 않은 리뷰 입니다."))
                    .andReturn()
            }

            it("when like already liked review should throw DuplicateLikeException") {
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

                reviewLikeRepository.save(
                    ReviewLike(
                        reviewId = review.id ?: 0,
                        userId = userRepository.findByEmail("test@gmail.com")?.id ?: 0
                    )
                )

                // when
                val result = mockMvc.perform(
                    post("$baseUrl/reviews/${review.id}/like")
                        .contentType("application/json")
                        .content(
                            objectMapper.writeValueAsString(
                                mapOf(
                                    "isLike" to true
                                )
                            )
                        )
                ).also {
                    println(it.andReturn().response.contentAsString)
                }
                    .andExpect(MockMvcResultMatchers.status().isBadRequest)
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result").value("FAIL"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(
                    Charset.forName(
                        "UTF-8"
                    )
                )
                val responseType =
                    object : TypeReference<CommonResponse<LikeReviewResponse>>() {}
                val actualResult: CommonResponse<LikeReviewResponse> =
                    objectMapper.readValue(
                        responseContent,
                        responseType
                    )

                // then
                actualResult.result shouldBe CommonResponse.Result.FAIL
                actualResult.message shouldBe "같은 게시글을 두번 좋아요할 수 없습니다."
            }

            it("when don't like review should cancel like") {
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
                            ?: throw Exception(),
                        likeCount = 1
                    )
                )

                reviewLikeRepository.save(
                    ReviewLike(
                        reviewId = review.id ?: 0,
                        userId = userRepository.findByEmail("test@gmail.com")
                            ?.id ?: 0
                    )
                )

                // when
                val result = mockMvc.perform(
                    post("$baseUrl/reviews/${review.id}/like")
                        .contentType("application/json")
                        .content(
                            objectMapper.writeValueAsString(
                                mapOf(
                                    "isLike" to false
                                )
                            )
                        )
                ).also {
                    println(it.andReturn().response.contentAsString)
                }
                    .andExpect(MockMvcResultMatchers.status().isOk)
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(
                    Charset.forName(
                        "UTF-8"
                    )
                )
                val responseType =
                    object : TypeReference<CommonResponse<LikeReviewResponse>>() {}
                val actualResult: CommonResponse<LikeReviewResponse> =
                    objectMapper.readValue(
                        responseContent,
                        responseType
                    )

                // then
                actualResult.data!!.review.isLike shouldBe false
            }

            it("when don't like not liked review should throw NotFoundLikeException") {
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

                // when
                val result = mockMvc.perform(
                    post("$baseUrl/reviews/${review.id}/like")
                        .contentType("application/json")
                        .content(
                            objectMapper.writeValueAsString(
                                mapOf(
                                    "isLike" to false
                                )
                            )
                        )
                ).also {
                    println(it.andReturn().response.contentAsString)
                }
                    .andExpect(MockMvcResultMatchers.status().isBadRequest)
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result").value("FAIL"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(
                    Charset.forName(
                        "UTF-8"
                    )
                )
                val responseType =
                    object : TypeReference<CommonResponse<LikeReviewResponse>>() {}
                val actualResult: CommonResponse<LikeReviewResponse> =
                    objectMapper.readValue(
                        responseContent,
                        responseType
                    )

                // then
                actualResult.result shouldBe CommonResponse.Result.FAIL
                actualResult.message shouldBe "해당 게시글은 이미 좋아하지 않습니다."
            }
        }
    }
}

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
import com.restaurant.be.review.presentation.dto.GetMyReviewsResponse
import com.restaurant.be.review.presentation.dto.common.ReviewResponseDto
import com.restaurant.be.review.repository.ReviewRepository
import com.restaurant.be.user.repository.UserRepository
import io.kotest.matchers.shouldBe
import org.springframework.data.domain.Page
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.Charset

@IntegrationTest
@Transactional
class GetMyReviewControllerTest(
    private val mockMvc: MockMvc,
    private val userRepository: UserRepository,
    private val restaurantRepository: RestaurantRepository,
    private val reviewRepository: ReviewRepository
) : CustomDescribeSpec() {
    private val baseUrl = "/v1/restaurants/my-reviews"
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

        describe("#getMyReview basic test") {
            it("when existed my review get should return success") {
                // given
                val restaurant = restaurantRepository.save(
                    RestaurantUtil.generateRestaurantEntity(
                        name = "restaurant"
                    )
                )

                for (i in 1..5) {
                    reviewRepository.save(
                        ReviewUtil.generateReviewEntity(
                            restaurantId = restaurant.id,
                            user = userRepository.findByEmail("test@gmail.com")
                                ?: throw Exception()
                        )
                    )
                }

                // when
                val result = mockMvc.perform(
                    get("$baseUrl")
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
                    object : TypeReference<CommonResponse<GetMyReviewsResponse>>() {}
                val actualResult: CommonResponse<GetMyReviewsResponse> =
                    objectMapper.readValue(
                        responseContent,
                        responseType
                    )

                // then
                actualResult.result shouldBe CommonResponse.Result.SUCCESS
                actualResult.data!!.reviews.content.size shouldBe 5
            }
        }

        describe("#getMyReview pagination test") {
            it("when 8 data and set size 5 and page 0 should return 5") {
                // given
                val restaurant = restaurantRepository.save(
                    RestaurantUtil.generateRestaurantEntity(
                        name = "restaurant"
                    )
                )

                for (i in 1..8) {
                    reviewRepository.save(
                        ReviewUtil.generateReviewEntity(
                            restaurantId = restaurant.id,
                            user = userRepository.findByEmail("test@gmail.com")
                                ?: throw Exception()
                        )
                    )
                }

                // when
                val result = mockMvc.perform(
                    get("$baseUrl")
                        .param("size", "5")
                        .param("page", "0")
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
                    object : TypeReference<CommonResponse<GetMyReviewsResponse>>() {}
                val actualResult: CommonResponse<GetMyReviewsResponse> =
                    objectMapper.readValue(
                        responseContent,
                        responseType
                    )

                // then
                actualResult.result shouldBe CommonResponse.Result.SUCCESS
                actualResult.data!!.reviews.content.size shouldBe 5
            }

            it("when 8 data and set size 5 and page 1 should return 5") {
                // given
                val restaurant = restaurantRepository.save(
                    RestaurantUtil.generateRestaurantEntity(
                        name = "restaurant"
                    )
                )

                for (i in 1..8) {
                    reviewRepository.save(
                        ReviewUtil.generateReviewEntity(
                            restaurantId = restaurant.id,
                            user = userRepository.findByEmail("test@gmail.com")
                                ?: throw Exception()
                        )
                    )
                }

                // when
                val result = mockMvc.perform(
                    get("$baseUrl")
                        .param("size", "5")
                        .param("page", "1")
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
                    object : TypeReference<CommonResponse<GetMyReviewsResponse>>() {}
                val actualResult: CommonResponse<GetMyReviewsResponse> =
                    objectMapper.readValue(
                        responseContent,
                        responseType
                    )

                // then
                actualResult.result shouldBe CommonResponse.Result.SUCCESS
                actualResult.data!!.reviews.content.size shouldBe 3
            }
        }

        describe("#getMyReview sort test") {
            it("when basic sort should return sorted data by id") {
                // given
                val restaurant = restaurantRepository.save(
                    RestaurantUtil.generateRestaurantEntity(
                        name = "restaurant"
                    )
                )

                for (i in 1..5) {
                    reviewRepository.save(
                        ReviewUtil.generateReviewEntity(
                            restaurantId = restaurant.id,
                            user = userRepository.findByEmail("test@gmail.com")
                                ?: throw Exception()
                        )
                    )
                }

                // when
                val result = mockMvc.perform(
                    get("$baseUrl")
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
                    object : TypeReference<CommonResponse<GetMyReviewsResponse>>() {}
                val actualResult: CommonResponse<GetMyReviewsResponse> =
                    objectMapper.readValue(
                        responseContent,
                        responseType
                    )

                // then
                actualResult.result shouldBe CommonResponse.Result.SUCCESS
                actualResult.data!!.reviews.content[0].id shouldBe actualResult.data!!.reviews.content[1].id + 1
                actualResult.data!!.reviews.content[1].id shouldBe actualResult.data!!.reviews.content[2].id + 1
                actualResult.data!!.reviews.content[2].id shouldBe actualResult.data!!.reviews.content[3].id + 1
                actualResult.data!!.reviews.content[3].id shouldBe actualResult.data!!.reviews.content[4].id + 1
            }

            it("when likeCount desc sort should return sorted data by likeCount") {
                // given
                val restaurant = restaurantRepository.save(
                    RestaurantUtil.generateRestaurantEntity(
                        name = "restaurant"
                    )
                )

                for (i in 1..5) {
                    reviewRepository.save(
                        ReviewUtil.generateReviewEntity(
                            restaurantId = restaurant.id,
                            user = userRepository.findByEmail("test@gmail.com")
                                ?: throw Exception(),
                            likeCount = 5 - i.toLong() + 1
                        )
                    )
                }

                // when
                val result = mockMvc.perform(
                    get("$baseUrl")
                        .param("sort", "likeCount,desc")
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
                    object : TypeReference<CommonResponse<GetMyReviewsResponse>>() {}
                val actualResult: CommonResponse<GetMyReviewsResponse> =
                    objectMapper.readValue(
                        responseContent,
                        responseType
                    )

                // then
                actualResult.result shouldBe CommonResponse.Result.SUCCESS
                actualResult.data!!.reviews.content[0].likeCount shouldBe 5
                actualResult.data!!.reviews.content[1].likeCount shouldBe 4
                actualResult.data!!.reviews.content[2].likeCount shouldBe 3
                actualResult.data!!.reviews.content[3].likeCount shouldBe 2
                actualResult.data!!.reviews.content[4].likeCount shouldBe 1
            }

            it("when likeCount asc sort should return sorted data by likeCount") {
                // given
                val restaurant = restaurantRepository.save(
                    RestaurantUtil.generateRestaurantEntity(
                        name = "restaurant"
                    )
                )

                for (i in 1..5) {
                    reviewRepository.save(
                        ReviewUtil.generateReviewEntity(
                            restaurantId = restaurant.id,
                            user = userRepository.findByEmail("test@gmail.com")
                                ?: throw Exception(),
                            likeCount = i.toLong()
                        )
                    )
                }

                // when
                val result = mockMvc.perform(
                    get("$baseUrl")
                        .param("sort", "likeCount,asc")
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
                    object : TypeReference<CommonResponse<GetMyReviewsResponse>>() {}
                val actualResult: CommonResponse<GetMyReviewsResponse> =
                    objectMapper.readValue(
                        responseContent,
                        responseType
                    )

                // then
                actualResult.result shouldBe CommonResponse.Result.SUCCESS
                actualResult.data!!.reviews.content[0].likeCount shouldBe 1
                actualResult.data!!.reviews.content[1].likeCount shouldBe 2
                actualResult.data!!.reviews.content[2].likeCount shouldBe 3
                actualResult.data!!.reviews.content[3].likeCount shouldBe 4
                actualResult.data!!.reviews.content[4].likeCount shouldBe 5
            }
        }
    }
}

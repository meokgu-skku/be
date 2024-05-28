package com.restaurant.be.restaurant.presentation.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.restaurant.be.common.CustomDescribeSpec
import com.restaurant.be.common.IntegrationTest
import com.restaurant.be.common.PageDeserializer
import com.restaurant.be.common.response.CommonResponse
import com.restaurant.be.common.util.RestaurantUtil
import com.restaurant.be.common.util.setUpUser
import com.restaurant.be.restaurant.presentation.controller.dto.RecommendRestaurantResponse
import com.restaurant.be.restaurant.presentation.controller.dto.common.RestaurantDto
import com.restaurant.be.restaurant.repository.RestaurantRepository
import com.restaurant.be.user.repository.UserRepository
import io.kotest.matchers.shouldBe
import org.springframework.data.domain.Page
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.Charset

@IntegrationTest
@Transactional
class RecommendRestaurantControllerTest(
    private val mockMvc: MockMvc,
    private val userRepository: UserRepository,
    private val redisTemplate: RedisTemplate<String, String>,
    private val restaurantRepository: RestaurantRepository
) : CustomDescribeSpec() {
    private val baseUrl = "/v1/restaurants/recommend"
    private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule()).apply {
        val module = SimpleModule()
        module.addDeserializer(Page::class.java, PageDeserializer(RestaurantDto::class.java))
        this.registerModule(module)
    }

    init {
        beforeEach {
            setUpUser("test@gmail.com", userRepository)
        }

        afterEach {
            redisTemplate.keys("*").forEach { redisTemplate.delete(it) }
        }

        describe("#getRecommendRestaurants") {
            it("when 5 restaurant saved should return recommended 5 restaurants") {
                // given
                val userId = userRepository.findByEmail("test@gmail.com")?.id ?: 0

                val restaurantIds = (1..5).map { i ->
                    val restaurant = restaurantRepository.save(
                        RestaurantUtil.generateRestaurantEntity(
                            name = "restaurant$i"
                        )
                    )

                    restaurant.id
                }

                val key = "RECOMMENDATION:$userId"
                redisTemplate.opsForValue().set(key, restaurantIds.joinToString(","))

                // when
                val result = mockMvc.perform(
                    MockMvcRequestBuilders.get(baseUrl)
                ).also {
                    println(it.andReturn().response.contentAsString)
                }
                    .andExpect(MockMvcResultMatchers.status().isOk)
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<RecommendRestaurantResponse>>() {}
                val actualResult: CommonResponse<RecommendRestaurantResponse> =
                    objectMapper.readValue(
                        responseContent,
                        responseType
                    )

                // then
                actualResult.data!!.restaurants.size shouldBe 5
            }

            it("when 7 restaurant saved should return recommended 5 restaurants") {
                // given
                val userId = userRepository.findByEmail("test@gmail.com")?.id ?: 0
                val restaurantIds = (1..7).map { i ->
                    val restaurant = restaurantRepository.save(
                        RestaurantUtil.generateRestaurantEntity(
                            name = "restaurant$i"
                        )
                    )

                    restaurant.id
                }

                val key = "RECOMMENDATION:$userId"
                redisTemplate.opsForValue().set(key, restaurantIds.joinToString(","))

                // when
                val result = mockMvc.perform(
                    MockMvcRequestBuilders.get(baseUrl)
                ).also {
                    println(it.andReturn().response.contentAsString)
                }
                    .andExpect(MockMvcResultMatchers.status().isOk)
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<RecommendRestaurantResponse>>() {}
                val actualResult: CommonResponse<RecommendRestaurantResponse> =
                    objectMapper.readValue(
                        responseContent,
                        responseType
                    )

                // then
                actualResult.data!!.restaurants.size shouldBe 5
            }

            it("when 3 restaurant saved should return recommended 3 restaurants") {
                // given
                val userId = userRepository.findByEmail("test@gmail.com")?.id ?: 0
                val restaurantIds = (1..3).map { i ->
                    val restaurant = restaurantRepository.save(
                        RestaurantUtil.generateRestaurantEntity(
                            name = "restaurant$i"
                        )
                    )

                    restaurant.id
                }

                val key = "RECOMMENDATION:$userId"
                redisTemplate.opsForValue().set(key, restaurantIds.joinToString(","))

                // when
                val result = mockMvc.perform(
                    MockMvcRequestBuilders.get(baseUrl)
                ).also {
                    println(it.andReturn().response.contentAsString)
                }
                    .andExpect(MockMvcResultMatchers.status().isOk)
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<RecommendRestaurantResponse>>() {}
                val actualResult: CommonResponse<RecommendRestaurantResponse> =
                    objectMapper.readValue(
                        responseContent,
                        responseType
                    )

                // then
                actualResult.data!!.restaurants.size shouldBe 3
            }

            it("when no restaurant saved in redis should recommended 5 restaurants") {
                // given
                val restaurantIds = (1..5).map { i ->
                    val restaurant = restaurantRepository.save(
                        RestaurantUtil.generateRestaurantEntity(
                            name = "restaurant$i"
                        )
                    )

                    restaurant.id
                }

                val key = "RECOMMENDATION:0"
                redisTemplate.opsForValue().set(key, restaurantIds.joinToString(","))

                // when
                val result = mockMvc.perform(
                    MockMvcRequestBuilders.get(baseUrl)
                ).also {
                    println(it.andReturn().response.contentAsString)
                }
                    .andExpect(MockMvcResultMatchers.status().isOk)
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<RecommendRestaurantResponse>>() {}
                val actualResult: CommonResponse<RecommendRestaurantResponse> =
                    objectMapper.readValue(
                        responseContent,
                        responseType
                    )

                // then
                actualResult.data!!.restaurants.size shouldBe 5
            }
        }
    }
}

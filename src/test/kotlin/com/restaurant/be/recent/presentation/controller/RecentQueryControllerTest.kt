package com.restaurant.be.recent.presentation.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.restaurant.be.common.CustomDescribeSpec
import com.restaurant.be.common.IntegrationTest
import com.restaurant.be.common.PageDeserializer
import com.restaurant.be.common.response.CommonResponse
import com.restaurant.be.common.util.setUpUser
import com.restaurant.be.recent.presentation.dto.RecentQueriesResponse
import com.restaurant.be.restaurant.presentation.controller.dto.common.RestaurantDto
import com.restaurant.be.user.repository.UserRepository
import io.kotest.matchers.shouldBe
import org.springframework.data.domain.Page
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.Charset

@IntegrationTest
@Transactional
class RecentQueryControllerTest(
    private val mockMvc: MockMvc,
    private val userRepository: UserRepository,
    private val redisTemplate: RedisTemplate<String, String>
) : CustomDescribeSpec() {
    private val baseUrl = "/v1/recents"
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

        describe("#getRecentQueries basic test") {
            it("when 5 recent queries saved should return 5 recent queries") {
                // given
                val userId = userRepository.findByEmail("test@gmail.com")?.id ?: 0
                redisTemplate.opsForList()
                    .rightPushAll(
                        "SR:$userId",
                        "query1",
                        "query2",
                        "query3",
                        "query4",
                        "query5"
                    )

                // when
                val result = mockMvc.perform(
                    get(baseUrl)
                ).also {
                    println(it.andReturn().response.contentAsString)
                }
                    .andExpect(MockMvcResultMatchers.status().isOk)
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<RecentQueriesResponse>>() {}
                val actualResult: CommonResponse<RecentQueriesResponse> =
                    objectMapper.readValue(
                        responseContent,
                        responseType
                    )

                // then
                actualResult.data!!.recentQueries.size shouldBe 5
            }

            it("when no recent queries saved should return empty list") {
                // when
                val result = mockMvc.perform(
                    get(baseUrl)
                ).also {
                    println(it.andReturn().response.contentAsString)
                }
                    .andExpect(MockMvcResultMatchers.status().isOk)
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<RecentQueriesResponse>>() {}
                val actualResult: CommonResponse<RecentQueriesResponse> =
                    objectMapper.readValue(
                        responseContent,
                        responseType
                    )

                // then
                actualResult.data!!.recentQueries.size shouldBe 0
            }

            it("when 7 recent queries saved should return 5 recent queries") {
                // given
                val userId = userRepository.findByEmail("test@gmail.com")?.id ?: 0
                redisTemplate.opsForList()
                    .rightPushAll(
                        "SR:$userId",
                        "query1",
                        "query2",
                        "query3",
                        "query4",
                        "query5",
                        "query6",
                        "query7"
                    )

                // when
                val result = mockMvc.perform(
                    get(baseUrl)
                ).also {
                    println(it.andReturn().response.contentAsString)
                }
                    .andExpect(MockMvcResultMatchers.status().isOk)
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<RecentQueriesResponse>>() {}
                val actualResult: CommonResponse<RecentQueriesResponse> =
                    objectMapper.readValue(
                        responseContent,
                        responseType
                    )

                // then
                actualResult.data!!.recentQueries.size shouldBe 5
            }
        }

        describe("#deleteRecentQueries basic test") {
            it("when 5 recent queries saved should delete all recent queries") {
                // given
                val userId = userRepository.findByEmail("test@gmail.com")?.id ?: 0
                redisTemplate.opsForList()
                    .rightPushAll(
                        "SR:$userId",
                        "query1",
                        "query2",
                        "query3",
                        "query4",
                        "query5"
                    )

                // when
                val result = mockMvc.perform(
                    delete(baseUrl)
                        .content(
                            objectMapper.writeValueAsString(
                                mapOf(
                                    "" to ""
                                )
                            )
                        )
                        .contentType("application/json")
                ).also {
                    println(it.andReturn().response.contentAsString)
                }
                    .andExpect(MockMvcResultMatchers.status().isOk)
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<RecentQueriesResponse>>() {}
                val actualResult: CommonResponse<RecentQueriesResponse> =
                    objectMapper.readValue(
                        responseContent,
                        responseType
                    )

                // then
                actualResult.data!!.recentQueries.size shouldBe 0
            }

            it("when 5 recent queries saved should delete specific recent query") {
                // given
                val userId = userRepository.findByEmail("test@gmail.com")?.id ?: 0
                redisTemplate.opsForList()
                    .rightPushAll(
                        "SR:$userId",
                        "query1",
                        "query2",
                        "query3",
                        "query4",
                        "query5"
                    )

                val result = mockMvc.perform(
                    delete(baseUrl)
                        .content(
                            objectMapper.writeValueAsString(
                                mapOf(
                                    "query" to "query3"
                                )
                            )
                        )
                        .contentType("application/json")
                ).also {
                    println(it.andReturn().response.contentAsString)
                }
                    .andExpect(MockMvcResultMatchers.status().isOk)
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<RecentQueriesResponse>>() {}
                val actualResult: CommonResponse<RecentQueriesResponse> =
                    objectMapper.readValue(
                        responseContent,
                        responseType
                    )

                // then
                actualResult.data!!.recentQueries.size shouldBe 4
            }
        }
    }
}

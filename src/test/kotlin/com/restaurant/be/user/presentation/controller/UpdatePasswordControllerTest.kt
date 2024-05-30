package com.restaurant.be.user.presentation.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.restaurant.be.common.CustomDescribeSpec
import com.restaurant.be.common.IntegrationTest
import com.restaurant.be.common.PageDeserializer
import com.restaurant.be.common.response.CommonResponse
import com.restaurant.be.common.util.setUpUser
import com.restaurant.be.restaurant.presentation.controller.dto.common.RestaurantDto
import com.restaurant.be.user.repository.UserRepository
import io.kotest.matchers.shouldBe
import org.springframework.data.domain.Page
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.Charset

@IntegrationTest
@Transactional
class UpdatePasswordControllerTest(
    private val mockMvc: MockMvc,
    private val userRepository: UserRepository,
    private val redisTemplate: RedisTemplate<String, String>
) : CustomDescribeSpec() {
    private val baseUrl = "/v1/users/password"
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

        describe("#passwordUpdate") {
            it("when token don't saved should return 400") {
                // given
                val email = userRepository.findByEmail("test@gmail.com")?.email ?: ""

                // when
                val result = mockMvc.perform(
                    patch(baseUrl)
                        .content(
                            objectMapper.writeValueAsString(
                                mapOf(
                                    "email" to email,
                                    "password" to "!!!QQQqqq111",
                                    "token" to "token"
                                )
                            )
                        )
                        .contentType("application/json")
                ).also {
                    println(it.andReturn().response.contentAsString)
                }
                    .andExpect(status().isBadRequest)
                    .andExpect(jsonPath("$.result").value("FAIL"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<Unit>>() {}
                val actualResult: CommonResponse<Unit> =
                    objectMapper.readValue(
                        responseContent,
                        responseType
                    )

                // then
                actualResult.result shouldBe CommonResponse.Result.FAIL
                actualResult.message shouldBe "유저가 비밀번호 초기화 상태가 아닙니다."
            }

            it("when token don't match should return 400") {
                // given
                val email = userRepository.findByEmail("test@gmail.com")?.email ?: ""
                redisTemplate.opsForValue().set("user:$email:reset_password_token", "tokenTest")

                // when
                val result = mockMvc.perform(
                    patch(baseUrl)
                        .content(
                            objectMapper.writeValueAsString(
                                mapOf(
                                    "email" to email,
                                    "password" to "!!!QQQqqq111",
                                    "token" to "token"
                                )
                            )
                        )
                        .contentType("application/json")
                ).also {
                    println(it.andReturn().response.contentAsString)
                }
                    .andExpect(status().isBadRequest)
                    .andExpect(jsonPath("$.result").value("FAIL"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<Unit>>() {}
                val actualResult: CommonResponse<Unit> =
                    objectMapper.readValue(
                        responseContent,
                        responseType
                    )

                // then
                actualResult.result shouldBe CommonResponse.Result.FAIL
                actualResult.message shouldBe "토큰이 일치 하지 않습니다."
            }

            it("when token saved should return 200") {
                // given
                val email = userRepository.findByEmail("test@gmail.com")?.email ?: ""
                redisTemplate.opsForValue().set("user:$email:reset_password_token", "tokenTest")

                // when
                val result = mockMvc.perform(
                    patch(baseUrl)
                        .content(
                            objectMapper.writeValueAsString(
                                mapOf(
                                    "email" to email,
                                    "password" to "!!!QQQqqq111",
                                    "token" to "tokenTest"
                                )
                            )
                        )
                        .contentType("application/json")
                ).also {
                    println(it.andReturn().response.contentAsString)
                }
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<Unit>>() {}
                val actualResult: CommonResponse<Unit> =
                    objectMapper.readValue(
                        responseContent,
                        responseType
                    )

                // then
                actualResult.result shouldBe CommonResponse.Result.SUCCESS
            }
        }
    }
}

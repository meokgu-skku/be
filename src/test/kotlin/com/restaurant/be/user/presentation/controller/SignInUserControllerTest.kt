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
import com.restaurant.be.user.presentation.dto.SignInUserResponse
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
class SignInUserControllerTest(
    private val mockMvc: MockMvc,
    private val userRepository: UserRepository
) : CustomDescribeSpec() {
    private val baseUrl = "/v1/users/email"
    private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule()).apply {
        val module = SimpleModule()
        module.addDeserializer(Page::class.java, PageDeserializer(RestaurantDto::class.java))
        this.registerModule(module)
    }

    init {
        beforeEach {
            setUpUser("test@gmail.com", userRepository)
        }

        describe("#signIn basic test") {
            it("when email and password is correct should return token") {
                // given
                val user = userRepository.findByEmail("test@gmail.com")!!

                // when
                val result = mockMvc.perform(
                    post("$baseUrl/sign-in")
                        .content(
                            objectMapper.writeValueAsString(
                                mapOf(
                                    "email" to user.email,
                                    "password" to "password"
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
                    object : TypeReference<CommonResponse<SignInUserResponse>>() {}
                val actualResult: CommonResponse<SignInUserResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.data!!.userDto.email shouldBe user.email
            }

            it("when email is not exist should throw NotFoundUserEmailException") {
                // given
                val notFoundEmail = "notfound@gmail.com"

                // when
                val result = mockMvc.perform(
                    post("$baseUrl/sign-in")
                        .content(
                            objectMapper.writeValueAsString(
                                mapOf(
                                    "email" to notFoundEmail,
                                    "password" to "test1234"
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
                    object : TypeReference<CommonResponse<SignInUserResponse>>() {}
                val actualResult: CommonResponse<SignInUserResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.message shouldBe "존재 하지 않는 유저 이메일 입니다."
            }

            it("when password is incorrect should throw InvalidPasswordException") {
                // given
                val user = userRepository.findByEmail("test@gmail.com")

                // when
                val result = mockMvc.perform(
                    post("$baseUrl/sign-in")
                        .content(
                            objectMapper.writeValueAsString(
                                mapOf(
                                    "email" to user?.email,
                                    "password" to "test1234"
                                )
                            )
                        )
                        .contentType("application/json")
                ).also {
                    println(it.andReturn().response.contentAsString)
                }
                    .andExpect(status().isUnauthorized)
                    .andExpect(jsonPath("$.result").value("FAIL"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<SignInUserResponse>>() {}
                val actualResult: CommonResponse<SignInUserResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.message shouldBe "패스워드가 일치 하지 않습니다."
            }
        }
    }
}

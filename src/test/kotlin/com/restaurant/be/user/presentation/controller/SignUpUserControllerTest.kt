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
import com.restaurant.be.user.presentation.dto.SignUpUserResponse
import com.restaurant.be.user.repository.UserRepository
import io.kotest.matchers.shouldBe
import org.springframework.data.domain.Page
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.Charset

@IntegrationTest
@Transactional
class SignUpUserControllerTest(
    private val mockMvc: MockMvc,
    private val userRepository: UserRepository
) : CustomDescribeSpec() {
    private val baseUrl = "/v1/users"
    private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule()).apply {
        val module = SimpleModule()
        module.addDeserializer(Page::class.java, PageDeserializer(RestaurantDto::class.java))
        this.registerModule(module)
    }

    init {
        beforeEach {
            setUpUser("test@gmail.com", userRepository)
        }

        describe("#signUpUser basic test") {
            it("when email and password is correct should return token") {
                // given
                val email = "newUser@gmail.com"
                val password = "password"
                val nickname = "newUser"
                val profileImageUrl = "profileImageUrl"

                // when
                val result = mockMvc.perform(
                    post("$baseUrl/email/sign-up")
                        .content(
                            objectMapper.writeValueAsString(
                                mapOf(
                                    "email" to email,
                                    "password" to password,
                                    "nickname" to nickname,
                                    "profileImageUrl" to profileImageUrl
                                )
                            )
                        )
                        .contentType("application/json")
                ).also {
                    println(it.andReturn().response.contentAsString)
                }.andExpect(status().isOk)
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<SignUpUserResponse>>() {}
                val actualResult: CommonResponse<SignUpUserResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.data!!.userDto.email shouldBe email
            }

            it("when existed nickname should throw DuplicateUserNicknameException") {
                // given
                val existedUserNickname =
                    userRepository.findByEmail("test@gmail.com")?.nickname ?: ""

                // when
                val result = mockMvc.perform(
                    post("$baseUrl/email/sign-up")
                        .content(
                            objectMapper.writeValueAsString(
                                mapOf(
                                    "email" to "test@test.com",
                                    "password" to "123456789",
                                    "nickname" to existedUserNickname,
                                    "profileImageUrl" to ""
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
                    object : TypeReference<CommonResponse<SignUpUserResponse>>() {}
                val actualResult: CommonResponse<SignUpUserResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.message shouldBe "이미 존재 하는 닉네임 입니다."
            }

            it("when existed email should throw DuplicateUserEmailException") {
                // given
                val existedUserEmail = "test@gmail.com"

                // when
                val result = mockMvc.perform(
                    post("$baseUrl/email/sign-up")
                        .content(
                            objectMapper.writeValueAsString(
                                mapOf(
                                    "email" to existedUserEmail,
                                    "password" to "123456789",
                                    "nickname" to "test",
                                    "profileImageUrl" to ""
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
                    object : TypeReference<CommonResponse<SignUpUserResponse>>() {}
                val actualResult: CommonResponse<SignUpUserResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.message shouldBe "이미 존재 하는 이메일 입니다."
            }
        }
    }
}
